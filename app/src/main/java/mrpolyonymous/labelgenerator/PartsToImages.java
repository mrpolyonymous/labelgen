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

import java.util.List;
import java.util.Map;

public class PartsToImages {
    private final Map<PartAndQuantitiesByColour, ImageInfo> partToImage;
    private final List<PartAndQuantitiesByColour> uniqueParts;
    private final List<PartAndQuantitiesByColour> missingImageParts;

    public PartsToImages(Map<PartAndQuantitiesByColour, ImageInfo> partToImage,
            List<PartAndQuantitiesByColour> uniqueParts,
            List<PartAndQuantitiesByColour> missingImageParts) {
        this.partToImage = partToImage;
        this.uniqueParts = uniqueParts;
        this.missingImageParts = missingImageParts;
    }

    public Map<PartAndQuantitiesByColour, ImageInfo> getPartToImage() {
        return partToImage;
    }

    public List<PartAndQuantitiesByColour> getUniqueParts() {
        return uniqueParts;
    }

    public List<PartAndQuantitiesByColour> getMissingImageParts() {
        return missingImageParts;
    }

}
