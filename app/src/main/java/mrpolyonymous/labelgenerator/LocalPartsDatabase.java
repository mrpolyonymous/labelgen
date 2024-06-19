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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import mrpolyonymous.rebrickableapi.ApiColour;
import mrpolyonymous.rebrickableapi.ApiElement;
import mrpolyonymous.rebrickableapi.ApiPart;

/**
 * A local database of parts, made by downloading data from the Rebrickable API.
 */
public class LocalPartsDatabase {
    private final File dataFolder;
    private final Path dataFolderPath;
    // parts DB file
    private final File partsDbFile;
    
    private Map<String, ApiPart> partInfos;
    private Map<Integer, ApiColour> colourInfos;
    private Map<String, ApiElement> elementInfos;
    private Map<String, File> imageUrlToLocalFile;

    public LocalPartsDatabase(File dataFolder) {
        if (!dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new IllegalArgumentException("Invalid data directory " + dataFolder);
        }
        try {
            dataFolderPath = dataFolder.getCanonicalFile().toPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot get real path of " + dataFolder, e);
        }

        this.dataFolder = dataFolder;
        this.partsDbFile = new File(dataFolder, "parts_db.json");
        
        // Tree maps so the database is somewhat sorted. Doesn't sort numbers correctly,
        // but still the order is better than a hashmap
        partInfos = new TreeMap<>();
        colourInfos = new TreeMap<>();
        elementInfos = new TreeMap<>();
        imageUrlToLocalFile = new TreeMap<>();
    }

    public void save() throws IOException {
        SerializedForm serializedForm = new SerializedForm();
        serializedForm.setParts(new ArrayList<>(partInfos.values()));
        serializedForm.setColours(new ArrayList<>(colourInfos.values()));
        serializedForm.setElements(elementInfos.values()
                .stream()
                .map(e -> {
                    ApiElement element = e;
                    return new SerializedElementInfo(
                            element.getElementId(),
                            element.getPart().getPartNum(),
                            String.valueOf(element.getColour().getId()),
                            element.getDesignId(),
                            element.getElementImgUrl(),
                            element.getPartImgUrl());
                })
                .collect(Collectors.toList()));
        
        List<ImageInfo> localImages = new ArrayList<>();
        for (Map.Entry<String, File> mapEntry : imageUrlToLocalFile.entrySet()) {
            File localFile = mapEntry.getValue();
            Path localPath = localFile.toPath();
            Path dataFolderPath = dataFolder.toPath();
            Path relativePath = dataFolderPath.relativize(localPath);
            ImageInfo imageInfo = new ImageInfo(mapEntry.getKey(), relativePath.toString());
            localImages.add(imageInfo);
        }

        serializedForm.setLocalImages(localImages);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(partsDbFile, serializedForm);
    }

    /**
     * Read the database from disk
     */
    public void read() throws IOException {
        elementInfos.clear();
        partInfos.clear();
        colourInfos.clear();
        imageUrlToLocalFile.clear();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SerializedForm serializedForm;
        try {
            serializedForm = objectMapper.readValue(partsDbFile, SerializedForm.class);
        } catch (FileNotFoundException e) {
            // No file means no data. Go on with life.
            return;
        }
        
        if (serializedForm.getParts() != null) {
            for (ApiPart partInfo : serializedForm.getParts()) {
                partInfos.put(partInfo.getPartNum(), partInfo);
            }
        }
        if (serializedForm.getColours() != null) {
            for (ApiColour colourInfo : serializedForm.getColours()) {
                colourInfos.put(colourInfo.getId(), colourInfo);
            }
        }
        if (serializedForm.getElements() != null) {
            for (SerializedElementInfo serializedInfo : serializedForm.getElements()) {
                ApiColour colour = colourInfos.get(Integer.valueOf(serializedInfo.colourId()));
                Objects.requireNonNull(colour, "Colour with ID " + serializedInfo.colourId() + " missing from database");
                ApiPart part = partInfos.get(serializedInfo.partId());
                Objects.requireNonNull(part, "Part with ID " + serializedInfo.partId() + " missing from database");
                
                ApiElement apiElement = new ApiElement();
                apiElement.setDesignId(serializedInfo.designId());
                apiElement.setElementId(serializedInfo.elementId());
                apiElement.setElementImgUrl(serializedInfo.elementImgUrl());
                apiElement.setPartImgUrl(serializedInfo.partImgUrl());
                apiElement.setColour(colour);
                apiElement.setPart(part);
                elementInfos.put(serializedInfo.elementId(), apiElement);
            }
        }
        
        if (serializedForm.getLocalImages() != null) {
            for (ImageInfo imageInfo : serializedForm.getLocalImages()) {
                validatePath(imageInfo.relativeFilePath());
                Path relativePath = Paths.get(imageInfo.relativeFilePath());
                Path absolutePath = dataFolderPath.resolve(relativePath).toAbsolutePath().normalize();
                if (!Utils.isChild(absolutePath, dataFolderPath)) {
                    throw new IllegalArgumentException("Bad image file path " + imageInfo.relativeFilePath());
                }
                if (!Files.isRegularFile(absolutePath)) {
                    System.out.println("Image " + imageInfo.imageUrl() + " cached at " + imageInfo.relativeFilePath() + " but file does not exist");
                    continue;
                }
                imageUrlToLocalFile.put(imageInfo.imageUrl(), absolutePath.toFile());
            }
        }
    }
    
    public boolean addPart(ApiPart part) {
        ApiPart currentPart = partInfos.get(part.getPartNum());
        // Don't replace parts
        if (currentPart != null) {
            return false;
        }
        
        partInfos.put(part.getPartNum(), part);
        return true;
    }

    public Collection<ApiPart> getParts() {
        return partInfos.values();
    }

    public boolean addColour(ApiColour colour) {
        colourInfos.put(colour.getId(), colour);
        // returns boolean for consistency
        return true;
    }

    public Collection<ApiColour> getColours() {
        return colourInfos.values();
    }

    public boolean addElement(ApiElement element) {
        ApiElement currentElement = elementInfos.get(element.getElementId());
        // Don't replace existing elements
        if (currentElement != null) {
            return false;
        }
        
        elementInfos.put(element.getElementId(), element);
        // Also break down the references inside the element into the relevant maps
        addPart(element.getPart());
        addColour(element.getColour());
        return true;
    }

    public Collection<ApiElement> getElements() {
        return elementInfos.values();
    }

    /**
     * A more efficient serialized form of an element that merely references the part and colour instead
     * of embedding the object directly
     */
    private static record SerializedElementInfo(String elementId,
            String partId,
            String colourId,
            String designId,
            String elementImgUrl,
            String partImgUrl) {
    }
    
    private static record ImageInfo(String imageUrl, String relativeFilePath) {}

    private static class SerializedForm {
        private List<SerializedElementInfo> elements;
        private List<ApiPart> parts;
        private List<ApiColour> colours;
        private List<ImageInfo> localImages;
        public List<SerializedElementInfo> getElements() {
            return elements;
        }
        public void setElements(List<SerializedElementInfo> elements) {
            this.elements = elements;
        }
        public List<ApiPart> getParts() {
            return parts;
        }
        public void setParts(List<ApiPart> parts) {
            this.parts = parts;
        }
        public List<ApiColour> getColours() {
            return colours;
        }
        public void setColours(List<ApiColour> colours) {
            this.colours = colours;
        }
        public List<ImageInfo> getLocalImages() {
            return localImages;
        }
        public void setLocalImages(List<ImageInfo> localImages) {
            this.localImages = localImages;
        }
        
    }

    public boolean hasElement(Element element) {    
        return elementInfos.containsKey(element.id());
    }

    public boolean hasPart(Part part) {
        return partInfos.containsKey(part.id());
    }
    
    void addIfRequired(ApiPart partInfo, String imageUrl, Set<String> imagesToFetch) {
        if (imageUrl == null) {
            return;
        }
        if (PartsToImageMapper.IGNORE_CATEGORIES.contains(String.valueOf(partInfo.getPartCatId()))) {
            return;
        }
        if (imageUrlToLocalFile.containsKey(imageUrl)) {
            return;
        }
        if (!imageUrl.startsWith(Fetcher.CDN_URL_BASE)) {
            System.out.println("Not fetching image " + imageUrl + " that does not belong to Rebrickable CDN");
            return;
        }
        imagesToFetch.add(imageUrl);
    }

    public void fetchImages(Fetcher fetcher) throws IOException {
        // Set of image URLs to be fetched
        Set<String> imagesToFetch = new TreeSet<>();
        
        for (ApiPart partInfo : partInfos.values()) {
            addIfRequired(partInfo, partInfo.getPartImgUrl(), imagesToFetch);
        }
        
        for (ApiElement elementInfo : elementInfos.values()) {

            // Elements have an element image URL, a part image URL, or both,
            // or sometimes rarely neither.
            // Prefer the element image URL if present.
            ApiElement element = elementInfo;
            String imageUrl = null;
            if (element.getElementImgUrl() == null) {
                if (element.getPartImgUrl() == null) {
//                    System.out.println("No image URL from element ID " + element.getElementId()
//                            + ", part="
//                            + element.getPart().getPartNum()
//                            + " "
//                            + element.getPart().getName()
//                            + ", and no part image either");
                } else {
                    imageUrl = element.getPartImgUrl();

                }
            } else {
                imageUrl = element.getElementImgUrl();
            }
            addIfRequired(elementInfo.getPart(), imageUrl, imagesToFetch);
        }
        
        CountDownLatch fetchLatch = new CountDownLatch(imagesToFetch.size());
        for (String imageUrl : imagesToFetch) {

            // Only support images in the Rebrickable CDN
            String shortImagePath = imageUrl.substring(Fetcher.CDN_URL_BASE.length());
            
            // Resolve the image URL relative to the data folder. Make sure it
            // makes a valid file name and is absolute.
            validatePath(shortImagePath);
            Path destPath = Paths.get(shortImagePath);
            destPath = dataFolderPath.resolve(destPath).toAbsolutePath().normalize();
            
            //System.out.println(destPath);
            if (!Utils.isChild(destPath, dataFolderPath)) {
                throw new IllegalArgumentException("Cannot create local path " + destPath + " for resource " + imageUrl);
            }
            Path containingFolder = destPath.getParent();
            if (!Files.exists(containingFolder)) {
                Files.createDirectories(containingFolder);
            }
            
            CompletableFuture<File> dlFuture;
            if (Files.isRegularFile(destPath)) {
                System.out.println("Local file for " + imageUrl + " already exists");
                dlFuture = CompletableFuture.completedFuture(destPath.toFile());
            } else {
                dlFuture = fetcher.fetchFromRebrickableCdnAsync(shortImagePath, destPath.toFile());
            }
            dlFuture.whenComplete((f, t) -> {
                if (t == null) {
                    synchronized (imageUrlToLocalFile) {
                        imageUrlToLocalFile.put(imageUrl, f);
                    }
                } else {
                    t.printStackTrace();
                }
                fetchLatch.countDown();
            });
        }
        
        if (fetchLatch.getCount() > 0) {
            try {
                System.out.println("Waiting for " + fetchLatch.getCount() + " fetches to complete");
                fetchLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final Pattern SAFE_PATH = Pattern.compile("^[-_a-zA-Z0-9\\\\\\./]+$");
    /**
     * Validate a path is potentially safe. This check is not sufficient, it needs
     * to be combined with real file system checks; this is just the first step.
     */
    private static void validatePath(String shortImagePath) {
        // These checks
        if (!SAFE_PATH.matcher(shortImagePath).matches()) {
            throw new IllegalArgumentException("Unsafe path: " + shortImagePath);
        }
        // Don't remember how to do this in a regex
        if (shortImagePath.contains("..")) {
            throw new IllegalArgumentException("Unsafe path: " + shortImagePath);
        }
    }

    public File getImageForPart(String partId) {
        ApiPart part = partInfos.get(partId);
        if (part == null) {
            return null;
        }
        
        String imageUrl = part.getPartImgUrl();
        if (imageUrl == null) {
            return null;
        }
        File imageFile = imageUrlToLocalFile.get(imageUrl);
        return imageFile;
    }
}
