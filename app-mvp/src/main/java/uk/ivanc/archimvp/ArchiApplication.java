package uk.ivanc.archimvp;

import android.app.Application;
import android.content.Context;

import uk.ivanc.archimvp.model.GithubService;

public class ArchiApplication extends Application {

    private GithubService githubService;

    public static ArchiApplication get(Context context) {
        return (ArchiApplication) context.getApplicationContext();
    }

    public GithubService getGithubService() {
        if (githubService == null) {
            githubService = GithubService.Factory.create();
        }
        return githubService;
    }
}