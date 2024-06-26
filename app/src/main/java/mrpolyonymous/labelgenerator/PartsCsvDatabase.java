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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * In-memory database of parts read from Rebrickable CSV data files
 */
public class PartsCsvDatabase {

    private static record PartColourId(String partId, String colourId) {}
    
    private Map<String, Colour> colours;
    private Map<String, PartCategory> partCategories;
    private Map<String, Part> parts;
    /** Map element ID to element data */
    private Map<String, Element> elements;
    /** Map part ID to count of number of elements that reference that part */
    private Map<String, Integer> elementCountsByPart;
    /** Map part and colour ID to element */
    private Map<PartColourId, Element> elementByPartColour;

    public PartsCsvDatabase() {
        colours = new HashMap<>();
        partCategories = new HashMap<>();
        parts = new HashMap<>();
        elements = new HashMap<>();
        elementCountsByPart = new HashMap<String, Integer>();
        elementByPartColour = new HashMap<>();
    }

    public void readColours(File dataFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line id,name,rgb,is_trans
            while ((line = br.readLine()) != null) {
                String[] elems = Utils.splitCsv(line, 4);
                Colour colour = new Colour(elems[0], elems[1]);
                colours.put(elems[0], colour);
            }
        }
    }

    public void readPartCategories(File dataFile) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line id,name
            while ((line = br.readLine()) != null) {
                String[] elems = Utils.splitCsv(line, 2);
                partCategories.put(elems[0], new PartCategory(elems[0], elems[1]));
            }
        }
    }

    public void readFullPartsList(File dataFile) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line part_num,name,part_cat_id,part_material
            while ((line = br.readLine()) != null) {
                String[] elems = Utils.splitCsv(line, 4);
                // rebrickable started adding leading zeros to numeric part ids under a 
                // certain length, which can mess up part matching. Trim them
                // down to the actual part ID.
                //elems[0] = Utils.trimLeadingZeros(elems[0]);
                parts.put(elems[0], Part.from(elems[0], elems[1], elems[2]));
                
                // elems[3] is part material, which we ignore
            }
        }
    }
    
    public void readElements(File dataFile) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line element_id,part_num,color_id,design_id
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] elems = Utils.splitCsv(line, 4);
                String partId = elems[1]; //Utils.trimLeadingZeros(elems[1]);
                Element element = new Element(elems[0], partId, elems[2], elems[3]);
                elements.put(elems[0], element);
                elementByPartColour.put(new PartColourId(partId, elems[2]), element);
                
                elementCountsByPart.compute(partId, (k,v)->{
                    if (v == null) {
                        return 1;
                    } else {
                        return 1 + v.intValue();
                    }
                });
            }
        }
    }

    public Part getPartById(String partId) {
        Part part = parts.get(partId);
        if (part == null) {
            throw new NoSuchElementException("No part found with ID " + partId);
        }
        return part;
    }

    public int getElementCountByPartId(String partId) {
        Integer count = elementCountsByPart.get(partId);
        if (count == null) {
            throw new NoSuchElementException("No part found with ID " + partId);
        }
        return count.intValue();
    }

    public Colour getColourById(String colourId) {
        Colour colour = colours.get(colourId);
        if (colour == null) {
            throw new NoSuchElementException("No colour found with ID " + colourId);
        }
        return colour;
    }

    public PartCategory getCategoryById(String partCategoryId) {
        PartCategory category = partCategories.get(partCategoryId);
        if (category == null) {
            throw new NoSuchElementException("No part category found with ID " + partCategoryId);
        }
        return category;
    }

    public Part tryGetPartByDescription(String description) {
        for (Part part : parts.values()) {
            if (part.description().equalsIgnoreCase(description)) {
                return part;
            }
        }
        return null;
    }

    public Part tryGetPartByIdPrefix(String idPrefix) {
        for  (Map.Entry<String, Part> mapEntry : parts.entrySet()) {
            if (mapEntry.getKey().startsWith(idPrefix)) {
                return mapEntry.getValue();
            }
        }
        return null;
    }

    public Element getElementById(String id) {
        Element element = elements.get(id);
        if (element == null) {
            throw new NoSuchElementException("No element found with ID " + id);
        }
        return element;
    }

    /**
     * Get an element by part and colour ID, throwing NoSuchElementException if not found
     * @throws  NoSuchElementException if the element is not found
     * 
     */
    public Element getElementByPartColourId(String partId, String colourId) {
        Element element = elementByPartColour.get(new PartColourId(partId, colourId));
        if (element == null) {
            throw new NoSuchElementException("No element found for part ID " + partId + " and colour ID " + colourId);
        }
        return element;
    }

    /**
     * Get an element by part and colour ID, returning null if not found
     */
    public Element tryGetElementByPartColourId(String partId, String colourId) {
        Element element = elementByPartColour.get(new PartColourId(partId, colourId));
        return element;
    }
}
