/*
Copyright 2024 mrpolyonymous

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package mrpolyonymous.labelgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class PartsToImageMapper {

    static final String COLOUR_BLACK = "0";
    /** Part categories where black is the preferred rendering colour */
    static final Set<String> PREFER_BLACK_CATEGORIES;

    /** Ordered preference for rendering colour when the original colour is not used/available */
    static final List<String> COLOUR_ORDER;

    /** Parts categories that will always be ignored */
    static final Set<String> IGNORE_CATEGORIES;
    
    /** Part categories where it is acceptable if an image is missing. */
    static final Set<String> IGNORE_IF_MISSING_CATEGORIES;

    /** Part categories where an attempt is made to get the exact image, mostly in the case of prints */
    static final Set<String> DONT_SIMPLIFY_IMAGE_CATEGORIES;

    /** Some parts have numbers that don't have an image, this maps them to
       a close-enough image. */
    static final Map<String, String> PART_NUM_TO_IMAGE_NUM;
    
    /** Colours known to be missing dedicated image files */
    static final Set<String> KNOWN_MISSING_COLOURS;
    static {
        PREFER_BLACK_CATEGORIES = new HashSet<>();
        PREFER_BLACK_CATEGORIES.add("53"); // Technic Pins
        PREFER_BLACK_CATEGORIES.add("46"); // Technic Axles
        PREFER_BLACK_CATEGORIES.add("29"); // Wheels and Tyres
        
        COLOUR_ORDER = new ArrayList<>();
        COLOUR_ORDER.add("71"); // light gray
        COLOUR_ORDER.add("72"); // dark gray
        COLOUR_ORDER.add("1"); // blue
        COLOUR_ORDER.add("4"); // red
        COLOUR_ORDER.add("14"); // yellow
        COLOUR_ORDER.add(COLOUR_BLACK);
        
        IGNORE_CATEGORIES = new HashSet<>();
        IGNORE_CATEGORIES.add("17"); // Non-LEGO
//        IGNORE_CATEGORIES.add("41"); // Bionicle
//        IGNORE_CATEGORIES.add("62"); // Minidoll Heads
//        IGNORE_CATEGORIES.add("64"); // Minidoll Lower Body
//        IGNORE_CATEGORIES.add("63"); // Minidoll Upper Body
        IGNORE_CATEGORIES.add("58"); // Stickers
        
        IGNORE_IF_MISSING_CATEGORIES = new HashSet<>();
        IGNORE_IF_MISSING_CATEGORIES.add("38"); // flags, plastic, cloth
        IGNORE_IF_MISSING_CATEGORIES.add("27"); // Minifig Accessories
        IGNORE_IF_MISSING_CATEGORIES.add("59"); // Minifig Heads
        IGNORE_IF_MISSING_CATEGORIES.add("65"); // Minifig Headwear
        IGNORE_IF_MISSING_CATEGORIES.add("61"); // Minifig Lower Body
        IGNORE_IF_MISSING_CATEGORIES.add("60"); // Minifig Upper Body
        IGNORE_IF_MISSING_CATEGORIES.add("45"); // Power Functions, Mindstorms and Electric
        IGNORE_IF_MISSING_CATEGORIES.add("31"); // String, Bands and Reels

        DONT_SIMPLIFY_IMAGE_CATEGORIES = new HashSet<>();
        DONT_SIMPLIFY_IMAGE_CATEGORIES.add("59"); // Minifig Heads
        DONT_SIMPLIFY_IMAGE_CATEGORIES.add("65"); // Minifig Headwear
        DONT_SIMPLIFY_IMAGE_CATEGORIES.add("61"); // Minifig Lower Body
        DONT_SIMPLIFY_IMAGE_CATEGORIES.add("60"); // Minifig Upper Body
        
        // These mappings were discovered by hand.
        // The mapping of standard part number to part number associated with the image seems
        // to be internal to rebrickable.
        PART_NUM_TO_IMAGE_NUM = new HashMap<>();
        PART_NUM_TO_IMAGE_NUM.put("19798", "3940"); //Support 2 x 2 x 2 Stand with Reinforced Underside
        PART_NUM_TO_IMAGE_NUM.put("47225", "47224c0"); //Pneumatic Cylinder with 2 Inlets and Rounded End Medium (48mm)
        PART_NUM_TO_IMAGE_NUM.put("73590c02a", "73590a"); // Flexible Hose 8.5L with Tabbed Ends (Ends same color as Tube)
        PART_NUM_TO_IMAGE_NUM.put("73983", "2429c01"); // Hinge Plate 1 x 4 Swivel Top / Base [Complete Assembly]
        PART_NUM_TO_IMAGE_NUM.put("76320c01", "32181c03"); // Technic Shock Absorber 10L, Damped, Normal Spring
        PART_NUM_TO_IMAGE_NUM.put("98560", "3684"); // Slope 75� 2 x 2 x 3 [Solid Studs]
        
        // Not exhaustive, just ones I've run across. Revisit periodically in case files become available.
        KNOWN_MISSING_COLOURS = new HashSet<String>();
        KNOWN_MISSING_COLOURS.add("1059"); // Opal Trans-Purple
        KNOWN_MISSING_COLOURS.add("1061"); // Opal Trans-Dark Blue
        KNOWN_MISSING_COLOURS.add("1088"); // Medium Brown
        KNOWN_MISSING_COLOURS.add("1089"); // Warm Tan
        KNOWN_MISSING_COLOURS.add("1092"); // Metallic Copper
        KNOWN_MISSING_COLOURS.add("1103"); // Pearl Titanium
        
    }

    private final File dataFolder;
    // output folder for all images
    private final File localImagesFolder;
    
    private final File ldrawBundleFolder;
    // For random part images that have been hand-downloaded
    private final String partImagesFolder;

    private final Set<Colour> missingColours;
    
    private final Fetcher fetcher;
    private final PartsDatabase partsDb;
    
	public PartsToImageMapper(File dataFolder, PartsDatabase partsDb, Fetcher fetcher) {
	    if (!dataFolder.exists() && !dataFolder.isDirectory()) {
	        throw new IllegalArgumentException("Invalid data directory " + dataFolder);
	    }

	    this.dataFolder = dataFolder;
	    this.partsDb = partsDb;
	    this.fetcher = fetcher;
		
		partImagesFolder = "part_images";
		localImagesFolder = new File(dataFolder, "local_images");
		localImagesFolder.mkdirs();
		
		ldrawBundleFolder = new File(dataFolder, "ldraw_bundles");
		ldrawBundleFolder.mkdirs();
		
		missingColours = new HashSet<>();
	}

	public PartsToImages mapPartsToImages(MyParts allMyParts) throws IOException {

		System.out.println("Number of part/colour combinations: " + allMyParts.size());
		fetchPartsImages(allMyParts);
		System.out.println("Missing images for colours: " + missingColours);
		
		List<PartAndQuantitiesByColour> uniqueParts = new ArrayList<>();
		PartAndQuantitiesByColour currentPart = null;
        allMyParts.sort();
		for (PartColourQuantity colouredPart: allMyParts) {
		    if (currentPart == null) {
                currentPart = new PartAndQuantitiesByColour(colouredPart.part());
                uniqueParts.add(currentPart);
		    } else if (!currentPart.part().id().equals(colouredPart.part().id())) {
		        // different IDs
		        // Don't care about different prints of the same part
		        if (colouredPart.part().id().contains("pr") && currentPart.part().idIgnoringPrint().equals(colouredPart.part().idIgnoringPrint())) {
		        } else {
		            currentPart = new PartAndQuantitiesByColour(colouredPart.part());
    				uniqueParts.add(currentPart);
    		    }
			}
			currentPart.addColourAndQuantity(colouredPart.colour(), colouredPart.quantity());
		}
		
		Map<String, ZipFile> partsFilesByColour = new HashMap<>();
		List<PartAndQuantitiesByColour> missingImageParts = new ArrayList<>();
		Map<PartAndQuantitiesByColour, ImageInfo> partToImage = new LinkedHashMap<>();
		int numPartsWithLocalImage = 0;
		int numOnes = 0;
        System.out.println("Finding images in ZIP files");
        Predicate<PartAndQuantitiesByColour> blanketIgnore = pandq -> {
            if (pandq.quantity() <= 1) {
                return false;
            }
            String categoryId = pandq.part().partCategoryId();
            if (IGNORE_CATEGORIES.contains(categoryId)) {
                return false;
            }
            return true;
        };
        Predicate<PartAndQuantitiesByColour> missingImageIgnore = pandq -> {
            if (pandq.quantity() <= 1) {
                return false;
            } else if (IGNORE_IF_MISSING_CATEGORIES.contains(pandq.part().partCategoryId())) {
                return false;
            } else if (pandq.part().id().contains("pr")) {
                // ignore unique prints
                return false;
            } else if (pandq.part().id().startsWith("upn") || pandq.part().id().startsWith("flex")) {
                return false;
            }
            return true;
        };

        for (PartAndQuantitiesByColour partAndQuantity : uniqueParts) {
		    if (partAndQuantity.quantity() <= 1) {
		        numOnes++;
		    }
		    if (!blanketIgnore.test(partAndQuantity)) {
		        continue;
		    }

		    Colour colour = partAndQuantity.colour();
		    ImageInfo imageInfo = null;
		    if ((colour.id().equals(COLOUR_BLACK) && !PREFER_BLACK_CATEGORIES.contains(partAndQuantity.part().partCategoryId())) ||
		            missingColours.contains(colour)) {
		        // For black parts or parts in missing colours, try to get an image that will be
		        // easier to see than the black version at small dimensions
		        imageInfo = getPreferredImage(partAndQuantity, COLOUR_ORDER, partsFilesByColour);
		    } else {
		        List<String> colourIds = new ArrayList<>(1+COLOUR_ORDER.size());
		        colourIds.add(colour.id());
		        colourIds.addAll(COLOUR_ORDER);
		        imageInfo = getPreferredImage(partAndQuantity, colourIds, partsFilesByColour);
		    }
		    		    
		    if (imageInfo == null) {
                missingImageParts.add(partAndQuantity);
                if (missingImageIgnore.test(partAndQuantity)) {
                    System.out.println("No image found for part " + partAndQuantity.part() + " in colour " + partAndQuantity.colour());
                    partToImage.put(partAndQuantity, null);
                }
		    } else {
		        numPartsWithLocalImage++;
	            partToImage.put(partAndQuantity, imageInfo);
		    }
		}

        System.out.println("Number of unique parts: " + uniqueParts.size());
		uniqueParts = uniqueParts.stream().filter(blanketIgnore).collect(Collectors.toList());
        System.out.println("Number of important unique parts: " + uniqueParts.size());
		System.out.println("Number of parts with quantity<=1: " + numOnes);
        System.out.println("Number of parts missing local images: " + missingImageParts.size());
        missingImageParts = missingImageParts.stream().filter(blanketIgnore).filter(missingImageIgnore).collect(Collectors.toList());
        System.out.println("Number of important parts missing local images: " + missingImageParts.size());
        System.out.println("Number of parts with local images: " + numPartsWithLocalImage);
        System.out.println("Number of parts being saved as grid: " + partToImage.size());
        
//        Map<PartAndQuantitiesByColour, ImageInfo> partsAndImages = extractImages(partToImage);

		return new PartsToImages(partToImage, uniqueParts, missingImageParts);
	}
	
	private ImageInfo getPreferredImage(PartAndQuantitiesByColour partAndQuantities, List<String> colourIds, Map<String, ZipFile> partsFilesByColour) throws IOException {
	    List<String> idsToTry = new ArrayList<>();
	    final Part part = partAndQuantities.part();
	    idsToTry.add(part.id());
        if (PART_NUM_TO_IMAGE_NUM.containsKey(part.id())) {
            idsToTry.add(PART_NUM_TO_IMAGE_NUM.get(part.id()));
        }
	    if (!part.id().equals(part.idIgnoringPrint()) && !DONT_SIMPLIFY_IMAGE_CATEGORIES.contains(part.partCategoryId())) {
	        idsToTry.add(part.idIgnoringPrint());
	    }

        List<File> possiblePartsFiles = new ArrayList<>();
        possiblePartsFiles.add(new File(new File(dataFolder, partImagesFolder), part.id() + ".jpg"));
        possiblePartsFiles.add(new File(new File(dataFolder, partImagesFolder), part.id() + ".png"));
        for (File partImage: possiblePartsFiles) {
            if (partImage.exists() && partImage.isFile()) {
                File destFile = new File(localImagesFolder, partImage.getName());
                if (!destFile.exists()) {
                    System.out.println("Copying " + partImage);
                    FileInputStream fis = new FileInputStream(partImage);
                    FileOutputStream fos = new FileOutputStream(destFile);
                    fis.transferTo(fos);
                    fis.close();
                    fos.close();
                }
                return new ImageInfo(destFile, 250, 250);
            }
        }
	    
	    for (String colourId: colourIds) {
	        Colour colour = partsDb.getColourById(colourId);
            ZipFile partsFile = partsFilesByColour.computeIfAbsent(colour.id(), key -> {
                try {
                    return new ZipFile(localLdrawFileForColour(colour), ZipFile.OPEN_READ);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            for (String partId: idsToTry) {
                ZipEntry zipEntry = partsFile.getEntry(partId + ".png");
                if (zipEntry != null) {
                    partAndQuantities.setColour(colour);
                    ImageInfo imageInfo = extractImage(partAndQuantities, new ZipFileEntry(partsFile, zipEntry));
                    return imageInfo;
                }
            }
	    }
	    
        return null;
    }

//    private Map<PartAndQuantitiesByColour, ImageInfo> extractImages(Map<PartAndQuantitiesByColour, ZipFileEntry> partToImage) throws IOException {
//        System.out.println("Extracting images");
//        Map<PartAndQuantitiesByColour, ImageInfo> outputMap = new LinkedHashMap<>();
//
//        for(Map.Entry<PartAndQuantitiesByColour, ZipFileEntry> mapEntry : partToImage.entrySet()) {
//            PartAndQuantitiesByColour part = mapEntry.getKey();
//            ZipFileEntry zippedImageEntry = mapEntry.getValue();
//            
//            if (zippedImageEntry == null) {
//                outputMap.put(part, null);
//            } else {
//                File outputFile = new File(localImagesFolder, part.colour().id() + "_" + part.part().id() + ".png");
//                if (!outputFile.exists()) {
//                    System.out.println("Unzipping " + outputFile);
//                    InputStream is = zippedImageEntry.zipFile.getInputStream(zippedImageEntry.zipEntry);
//                    FileOutputStream fos = new FileOutputStream(outputFile);
//                    is.transferTo(fos);
//                    fos.close();
//                    is.close();
//                }
//                
////                BufferedImage bi = ImageIO.read(outputFile);
////                outputMap.put(part, new ImageInfo(outputFile, bi.getWidth(), bi.getHeight()));
//                outputMap.put(part, new ImageInfo(outputFile, 500, 500));
//            }
//        }
//        
//        return outputMap;
//    }

    private ImageInfo extractImage(PartAndQuantitiesByColour part, ZipFileEntry zipFileEntry) throws IOException {

        File outputFile = new File(localImagesFolder, part.colour().id() + "_" + part.part().id() + ".png");
        if (!outputFile.exists()) {
            System.out.println("Unzipping " + outputFile);
            InputStream is = zipFileEntry.zipFile().getInputStream(zipFileEntry.zipEntry());
            FileOutputStream fos = new FileOutputStream(outputFile);
            is.transferTo(fos);
            fos.close();
            is.close();
        }
        
        return new ImageInfo(outputFile, 500, 500);
    }

    
    private record ColourFetch(Colour colour, Future<File> fetchFuture) {}
    
	private void fetchPartsImages(Iterable<PartColourQuantity> allParts) {
		Set<String> usedColourIds = new TreeSet<>();
		
		for (PartColourQuantity part: allParts) {
			usedColourIds.add(part.colour().id());
		}
		
		List<ColourFetch> colourFetches = new ArrayList<>(usedColourIds.size());
		for (String colourId: usedColourIds) {
            Colour colour = partsDb.getColourById(colourId);
            colourFetches.add(new ColourFetch(colour, fetchPartsImagesIfRequired(colour)));
		}
		
		for (ColourFetch colourFetch : colourFetches) {
		    try {
		        Future<File> fetchFuture = colourFetch.fetchFuture();
		        if (fetchFuture == null) {
		            missingColours.add(colourFetch.colour());
		        } else {
		            fetchFuture.get();
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		        System.err.println("Could not fetch parts images for " + colourFetch.colour() + "; ignoring");
		        missingColours.add(colourFetch.colour());
		    }
		}
	}

    private Future<File> fetchPartsImagesIfRequired(Colour colour) {
        final File localFile = localLdrawFileForColour(colour);
        if (localFile.exists()) {
            System.out.println("Parts file for colour " + colour + " exists, not downloading.");
            return CompletableFuture.completedFuture(localFile);
        } else if (KNOWN_MISSING_COLOURS.contains(colour.id())) {
            System.out.println("No parts file for colour " + colour + " exists on rebrickable, skipping");
            return null;
        } else {
            return fetcher.fetchFromRebrickableCdnAsync("ldraw/parts_" + colour.id() + ".zip", localFile);
        }
    }

    private File localLdrawFileForColour(Colour colour) {
        return new File(ldrawBundleFolder, "parts_" + colour.id() + ".zip");
    }

}
