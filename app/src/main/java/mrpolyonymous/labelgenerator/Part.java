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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A part definition read from Rebrickable data, with some additional fields for convenience.
 * 
 */
public record Part(String id, String idIgnoringPrint, String description, String partCategoryId, Integer numericId) {

    private static final Pattern PRINT_PATTERN = Pattern.compile("^(.+)(pr|pat)[a-z]?\\d+$");
    public static String idIgnoringPrint(String id) {
        try {
            Integer.parseInt(id);
            return id;
        } catch (NumberFormatException e) {
        }

        Matcher m = PRINT_PATTERN.matcher(id);
        if (m.matches()) {
            // recursive call because some parts have both print and pattern and I'm too lazy 
            // to figure out the correct regex
            return idIgnoringPrint(m.group(1));
        }
        return id;
    }

    public static Part from(String id, String description, String partCategoryId) {

        String idIgnoringPrint = idIgnoringPrint(id);
        Integer numericId = null;
        try {
            numericId = Integer.parseInt(idIgnoringPrint);
        } catch (NumberFormatException e) {
        }

        return new Part(id, idIgnoringPrint, description, partCategoryId, numericId);
    }
}
