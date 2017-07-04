package uk.ivanc.archimvp.presenter;

import android.util.Log;

import uk.ivanc.archimvp.ArchiApplication;
import uk.ivanc.archimvp.service.GithubService;
import uk.ivanc.archimvp.model.User;
import uk.ivanc.archimvp.view.RepositoryMvpView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RepositoryPresenter implements Presenter<RepositoryMvpView> {

    private static final String TAG = "RepositoryPresenter";

    private RepositoryMvpView repositoryMvpView;
    private Disposable mDisposable;

    @Override
    public void attachView(RepositoryMvpView view) {
        this.repositoryMvpView = view;
    }

    @Override
    public void detachView() {
        this.repositoryMvpView = null;
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public void loadOwner(String userUrl) {
        ArchiApplication application = ArchiApplication.get(repositoryMvpView.getContext());
        GithubService githubService = application.getGithubService();
        mDisposable = githubService.userFromUrl(userUrl)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(application.defaultSubscribeScheduler())
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                        Log.i(TAG, "Full user data loaded " + user);
                        repositoryMvpView.showOwner(user);
                    }
                });
    }
}
