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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class OutputGenerator {

    private final PartsDatabase partsDatabase;

    public OutputGenerator(PartsDatabase partsDatabase) {
        this.partsDatabase = partsDatabase;
    }

    public void savePartsGrid(File outputFile, PartsToImages results) throws IOException {
        System.out.println("Saving parts grid");
        Map<PartAndQuantitiesByColour, ImageInfo> partsAndImages = results.getPartToImage();
        // Organize by category
        Map<PartCategory, List<PartAndImageInfo>> byCategory = new TreeMap<>(
                (cat1, cat2) -> cat1.description().compareTo(cat2.description()));
        for (Map.Entry<PartAndQuantitiesByColour, ImageInfo> mapEntry : partsAndImages.entrySet()) {
            PartAndQuantitiesByColour part = mapEntry.getKey();
            ImageInfo imageInfo = mapEntry.getValue();
            PartCategory partCategory = partsDatabase.getCategoryById(part.part().partCategoryId());
            Objects.requireNonNull(partCategory, "missing category");

            List<PartAndImageInfo> partList = byCategory.computeIfAbsent(partCategory, k -> new ArrayList<>());
            partList.add(new PartAndImageInfo(part, imageInfo));
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
            saveHtml(pw, byCategory);
        }

    }

    private void saveHtml(PrintWriter pw, Map<PartCategory, List<PartAndImageInfo>> byCategory) {
        pw.print("<!DOCTYPE html>\n" + "<html>\n"
                + "<head>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "\n");
        pw.print("<style>\n" + "@media print {\n"
                + "  div {\n"
                + "    break-inside: avoid;\n"
                + "  }\n"
                + "}\n"
                + ".category {\n"
                + "    clear: both;\n"
                + "    font-weight: bold;\n"
                + "    font-size: 0.5in;\n"
                + "    padding-bottom: 40px;\n"
                + "}\n"
                + ".category.later {\n"
                + "    padding-top: 80px;\n"
                + "}\n"
                + ".cell {\n"
                //+ "    overflow: auto;\n"
                + "    overflow: hidden;\n"
                + "    float: left;\n"
                + "    width: 1.5in;\n"
                + "    height: 0.5in;\n"
                + "    padding: 4px;\n"
                + "    border: 1px solid black;\n"
                + "    font-family: sans-serif;\n"
                + "    font-size: 6pt;\n"
                + "}\n"
                + ".cell img {\n"
                + "    float: left;\n"
                + "    margin-right: 4px;\n"
                + "    padding: 4px 0px;\n"
                //+ "    min-height: 50px;\n"
                + "}\n"
                + ".rare {\n"
                + "    background-color: #FFFFF0;\n"
                + "}\n"
                + ".cell .missing_image {\n"
                + "    float: left;\n"
                + "    margin-right: 5px;\n"
                + "    width: 40px;\n"
                + "    height: 40px;\n"
                + "}\n"
                + ".cell .label {font-weight: bold;}\n"
                + "</style>");
        pw.print("</head>\n" + "<body>\n" + "");

        int numCells = 0;
        int numCategories = 0;
        for (Map.Entry<PartCategory, List<PartAndImageInfo>> mapEntry : byCategory.entrySet()) {
            PartCategory category = mapEntry.getKey();
            List<PartAndImageInfo> parts = mapEntry.getValue();
            if (numCategories == 0) {
                pw.println("<div class=\"category\">");
            } else {
                pw.println("<div class=\"category later\">");
            }
            pw.println(category.description());
            pw.println("</div>");

            for (PartAndImageInfo partAndImageInfo : parts) {

                PartAndQuantitiesByColour part = partAndImageInfo.part();
                ImageInfo imageInfo = partAndImageInfo.imageInfo();
//                if (part.quantity() < 4) {
//                    continue;
//                    pw.println("<div class=\"cell rare\">");
//                } else {
                pw.println("<div class=\"cell\">");
//                }
                if (imageInfo == null) {
                    pw.println("<div class=\"missing_image\">?</div>");
                } else {
                    pw.println("<img src=\"" + imageInfo.path().toString().replace('\\', '/') + "\" width=\"40px\" />");
                }
                pw.println(Utils.trimToLength(part.part().description(), 50));
                pw.println("<br/>");
                pw.println("<span class=\"label\">ID:</span> " + part.part().id());
                pw.println("<br/>");

                pw.println("<span class=\"label\">Cat:</span> " + category.description());
                //pw.println("<br/>");
                //pw.println("<span class=\"label\">Quantity:</span> " + part.quantity());
                pw.println("</div>");

                ++numCells;
                if (numCells >= 10000) {
                    break;
                }
            }
            if (numCells >= 10000) {
                break;
            }
            ++numCategories;
        }

        pw.print("</body>\n" + "<html>\n");
    }

    public void savePartsCsv(String filename, List<PartAndQuantitiesByColour> parts) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("part,color,quantity");
            for (PartAndQuantitiesByColour p : parts) {
                pw.print(p.part().id());
                pw.print(",");
                pw.print(p.colour().id());
                pw.print(",");
                pw.print(p.quantity());
                pw.println();
            }
        }
    }

}
