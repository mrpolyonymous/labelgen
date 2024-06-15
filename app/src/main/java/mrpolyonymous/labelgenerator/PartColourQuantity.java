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

import java.util.Objects;

/**
 * An entry from a Rebrickable CSV parts list, which maps part, colour and quantity
 */
record PartColourQuantity(Part part, Colour colour, int quantity) {
    
    public static PartColourQuantity fromLine(String line, PartsDatabase partsDb) { //TODO Map<String, Part> idToPart, Map<String, Colour> idToColour) {
        String[] elems = Utils.splitCsv(line, 3);
        elems[0] = Utils.trimLeadingZeros(elems[0]);

        try {
            return new PartColourQuantity(partsDb.getPartById(elems[0]), partsDb.getColourById(elems[1]), Integer.parseInt(elems[2]));
        } catch (RuntimeException e) {
            System.err.println("Error making part ID=" + elems[0] + " colour ID=" + elems[1] + " qty=" + elems[2]);
            throw e;
        }
    }

    PartColourQuantity(Part part, Colour colour, int quantity) {
        Objects.requireNonNull(part, "part is required");
        Objects.requireNonNull(colour, "part colour is required");
        this.part = part;
        this.colour = colour;
        this.quantity = quantity;
    }
}
