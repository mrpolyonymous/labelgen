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
import java.util.TreeMap;

/**
 * In-memory database of parts read from rebrickable data
 */
public class PartsDatabase {

    private Map<String, Colour> colours;
    private Map<String, PartCategory> partCategories;
    private Map<String, Part> parts;

    public PartsDatabase() {
        colours = new HashMap<>();
        partCategories = new TreeMap<>();
        parts = new TreeMap<>();
    }

    public void readColours(File dataFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line id,name,rgb,is_trans
            line = br.readLine();
            while (line != null) {
                String[] elems = Utils.splitCsv(line, 4);
                Colour colour = new Colour(elems[0], elems[1]);
                colours.put(elems[0], colour);
                line = br.readLine();
            }
        }
    }

    public void readPartCategories(File dataFile) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line id,name
            line = br.readLine();
            while (line != null) {
                String[] elems = Utils.splitCsv(line, 2);
                partCategories.put(elems[0], new PartCategory(elems[0], elems[1]));
                line = br.readLine();
            }
        }
    }

    public void readFullPartsList(File dataFile) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // Skip header line part_num,name,part_cat_id,part_material
            line = br.readLine();
            while (line != null) {
                String[] elems = Utils.splitCsv(line, 4);
                // rebrickable started adding leading zeros to numeric part ids under a 
                // certain length, which can mess up part matching. Trim them
                // down to the actual part ID.
                elems[0] = Utils.trimLeadingZeros(elems[0]);
                parts.put(elems[0], Part.from(elems[0], elems[1], elems[2]));
                line = br.readLine();
                
                // elems[3] is part material, which we ignore
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
}
