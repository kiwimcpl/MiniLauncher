package me.minidigger.minecraftlauncher.launcher.tasks;

import org.to2mbn.jmccc.mcdownloader.CacheOption;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class VersionListUpdaterTask extends Thread {

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("minilauncher");
    private MinecraftDownloader minecraftDownloader;

    private ComboBox<String> optionsSelectVersion;
    private Button optionsSelectVersionInstall;
    private Consumer<String> statusConsumer;
    private Hashtable<String, String> VersionHashTable;
    private Runnable callback;

    public VersionListUpdaterTask(MinecraftDownloader minecraftDownloader, ComboBox<String> optionsSelectVersion,
                                  Button optionsSelectVersionInstall, Consumer<String> statusConsumer,
                                  Hashtable<String, String> versionHashTable, Runnable callback) {
        this.minecraftDownloader = minecraftDownloader;
        this.optionsSelectVersion = optionsSelectVersion;
        this.optionsSelectVersionInstall = optionsSelectVersionInstall;
        this.statusConsumer = statusConsumer;
        this.callback = callback;
        VersionHashTable = versionHashTable;

        setName("VersionListUpdaterTask");
    }

    @Override
    public void run() {
        minecraftDownloader.fetchRemoteVersionList(new CombinedDownloadCallback<RemoteVersionList>() {
            @Override
            public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
                statusConsumer.accept(resourceBundle.getString("status.getting_latest_version"));
                optionsSelectVersion.setDisable(true);
                optionsSelectVersionInstall.setDisable(true);

                return new CallbackAdapter<R>() {

                    @Override
                    public void done(R result) {
                        statusConsumer.accept(resourceBundle.getString("status.done"));
                    }

                    @Override
                    public void failed(Throwable e) {
                        statusConsumer.accept(resourceBundle.getString("status.download_failed"));
                    }

                    @Override
                    public void cancelled() {
                        statusConsumer.accept(resourceBundle.getString("status.cancelled"));
                    }

                    @Override
                    public void updateProgress(long done, long total) {
                        statusConsumer.accept(String.format(resourceBundle.getString("status.progress"), done, total));
                    }

                    @Override
                    public void retry(Throwable e, int current, int max) {
                        statusConsumer.accept(String.format(resourceBundle.getString("status.retry"), current, max));
                    }
                };
            }

            @Override
            public void done(RemoteVersionList result) {
                for (String key : result.getVersions().keySet()) {
                    optionsSelectVersion.getItems().addAll(key);
                    VersionHashTable.put(key, result.getVersions().get(key).getVersion());
                }

                optionsSelectVersion.setDisable(false);
                optionsSelectVersionInstall.setDisable(false);

                callback.run();
            }

            @Override
            public void failed(Throwable e) {
                statusConsumer.accept(resourceBundle.getString("status.getting_latest_version_failed"));
                e.printStackTrace();
            }

            @Override
            public void cancelled() {

            }
        }, CacheOption.CACHE);
    }
}