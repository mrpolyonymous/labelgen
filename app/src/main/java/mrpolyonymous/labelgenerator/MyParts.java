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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parts downloaded from Rebrickable and matched to their corresponding part, colour and quantity
 */
public class MyParts implements Iterable<PartColourQuantity> {

    /**
     * Entries read and parsed from CSV file. It is possible for there to be duplicates, Rebrickable
     * does not enforce uniqueness.
     */
    private final List<PartColourQuantity> allParts;

    static record PartDetails(Part part, Colour colour, Element element, int quantity) {
    }

    private final Map<String, List<PartDetails>> partsMap;

    private final PartsCsvDatabase partsDatabase;

    public MyParts(PartsCsvDatabase partsDatabase) {
        this.partsDatabase = partsDatabase;

        allParts = new ArrayList<>(8000);
        partsMap = new HashMap<>(8000);
    }

    /**
     * Read data file downloaded from rebrickable My LEGO->All My Parts->Export Parts->Rebrickable CSV
     */
    public void readMyParts(File dataFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine();
            // skip header  Part,Color,Quantity
            while ((line = br.readLine()) != null) {
                PartColourQuantity newPart = PartColourQuantity.fromLine(line, partsDatabase);
                add(newPart);
            }
        }

//        sort();
    }

    private static final Comparator<PartColourQuantity> SORTER = (p1, p2) -> {

        int partIdComp;
        if (p1.part().numericId() != null && p2.part().numericId() != null) {
            partIdComp = p1.part().numericId().compareTo(p2.part().numericId());
        } else {
            partIdComp = p1.part().idIgnoringPrint().compareTo(p2.part().idIgnoringPrint());
        }
        if (partIdComp != 0) {
            return partIdComp;
        }
        return p1.part().id().compareTo(p2.part().id());
    };

    public int size() {
        return allParts.size();
    }

    @Override
    public Iterator<PartColourQuantity> iterator() {
        return Collections.unmodifiableList(allParts).iterator();
    }

    void add(PartColourQuantity pcq) {
        allParts.add(pcq);

        List<PartDetails> details = partsMap.computeIfAbsent(pcq.part().id(), k -> new ArrayList<>());

        boolean added = false;
        // Look for duplicate entries, and if found combine the quantities
        if (!details.isEmpty()) {
            Iterator<PartDetails> it = details.iterator();
            while (it.hasNext()) {
                PartDetails partDetails = it.next();
                if (partDetails.colour().equals(pcq.colour())) {
                    // duplicate entry, add together quantities.
                    it.remove();
                    details.add(new PartDetails(partDetails.part(), partDetails.colour(),
                            partDetails.element(),
                            partDetails.quantity() + pcq.quantity()));
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            // No duplicate found
            details.add(new PartDetails(pcq.part(),
                    pcq.colour(),
                    partsDatabase.tryGetElementByPartColourId(pcq.part().id(), pcq.colour().id()),
                    pcq.quantity()));
        }
    }

    public void sort() {
        allParts.sort(SORTER);
    }

    public int coloursForPart(Part part) {
        List<PartDetails> details = partsMap.get(part.id());
        if (details == null) {
            return 0;
        }
        return details.size();
    }
    
    public Map<String, List<PartDetails>> getPartsMap() {
        return partsMap;
    }
    
    public Part getPartForId(String id) {
        List<PartDetails> partDetailsList = partsMap.get(id);
        if (partDetailsList == null) {
            return null;
        }
        return partDetailsList.get(0).part();
    }
}
