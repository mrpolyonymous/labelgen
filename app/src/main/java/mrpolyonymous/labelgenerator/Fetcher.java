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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class Fetcher implements AutoCloseable {

    private final ExecutorService fetcherService;
    public Fetcher() {
        // small thread pool to not put too much stress on internet connection or rebrickable
        fetcherService = Executors.newFixedThreadPool(3);
    }

    @Override
    public void close() {
        fetcherService.shutdown();
        
    }

    static final String CDN_URL_BASE = "https://cdn.rebrickable.com/media/";

    class FetchFromRebrickableCdn implements Callable<File> {
        private final String cdnFileName;
        private final File destinationFile;

        public FetchFromRebrickableCdn(String cdnFileName, File destinationFile) {
            this.cdnFileName = cdnFileName;
            this.destinationFile = destinationFile;
        }

        @Override
        public File call() throws Exception {
            URL urlToFetch = new URL(CDN_URL_BASE + cdnFileName);
            System.out.println("Download " + urlToFetch + " to " + destinationFile.getAbsolutePath());
            HttpsURLConnection urlConnection = (HttpsURLConnection) urlToFetch.openConnection();
            try (InputStream is = urlConnection.getInputStream();
                    FileOutputStream os = new FileOutputStream(destinationFile)) {
                is.transferTo(os);
            }
            return destinationFile;
        }
    }

    Future<File> fetchFromRebrickableCdnDownloadsAsync(String cdnFileName, File destFile) {
        Future<File> downloadFuture = fetcherService.submit(new FetchFromRebrickableCdn("downloads/" + cdnFileName, destFile));
        return downloadFuture;
    }

    CompletableFuture<File> fetchFromRebrickableCdnAsync(String cdnFileName, File destFile) {
        CompletableFuture<File> downloadFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return new FetchFromRebrickableCdn(cdnFileName, destFile).call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, fetcherService);
        return downloadFuture;
    }

}
