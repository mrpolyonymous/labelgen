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

public class Utils {

    /**
     * Split a line from a CSV file into an expected number of columns.
     * <p>
     * Not how fully generic CSV parsing should be done, but works with rebrickable data
     * without requiring an additional dependency like Apache commons-csv
     */
    static String[] splitCsv(String line, int numColumns) {
        String[] elems = new String[numColumns];
        StringBuilder sb = new StringBuilder();
        
        int numElems = 0;
        boolean inQuote = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',') {
                if (inQuote) {
                    sb.append(c);
                } else {
                    elems[numElems++] = sb.toString();
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
            ++i;
        }
        elems[numElems++] = sb.toString();
        
        if (numElems != numColumns) {
            throw new RuntimeException("Failed to parse " + numColumns + " columns from: " + line);
        }
        return elems;
    }

    /**
     * Trim leading '0' characters from a string
     */
    static String trimLeadingZeros(String id) {
        // Not the most efficient way of doing it
        // check for length > 1 so that "0" doesn't get trimmed to ""
        while (id.startsWith("0") && id.length() > 1) {
            id = id.substring(1);
        }
        return id;
    }
    
    static String trimToLength(String s, int maxLength) {
        if (s.length() <= maxLength) {
            return s;
        }
        int upToSpace = maxLength;
        while (!Character.isWhitespace(s.charAt(upToSpace)) && upToSpace > 0) {
            --upToSpace;
        }
        return s.substring(0, upToSpace).strip();
    }


}
