package uk.ivanc.archimvp;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import uk.ivanc.archimvp.model.Repository;
import uk.ivanc.archimvp.presenter.MainPresenter;
import uk.ivanc.archimvp.rule.ImmediateSchedulersRule;
import uk.ivanc.archimvp.service.GithubService;
import uk.ivanc.archimvp.util.MockModelFabric;
import uk.ivanc.archimvp.view.MainMvpView;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava2.HttpException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainPresenterTest {

    @Rule
    public final ImmediateSchedulersRule mSchedulersRule = new ImmediateSchedulersRule();
    MainPresenter mainPresenter;
    MainMvpView mainMvpView;
    GithubService githubService;

    @Before
    public void setUp() {
        // given:
        ArchiApplication application = (ArchiApplication) RuntimeEnvironment.application;
        githubService = mock(GithubService.class);
        // Mock the retrofit service so we don't call the API directly
        application.setGithubService(githubService);
        // Change the default subscribe schedulers so all observables
        // will now run on the same thread

//        RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
//            @Override
//            public Scheduler apply(Scheduler scheduler) throws Exception {
//                return Schedulers.trampoline();
//            }
//        });
        application.setDefaultSubscribeScheduler(Schedulers.io());
        mainPresenter = new MainPresenter();
        mainMvpView = mock(MainMvpView.class);

        // when:
        when(mainMvpView.getContext()).thenReturn(application);

        // then:
        mainPresenter.attachView(mainMvpView);
    }

    @After
    public void tearDown() {
        mainPresenter.detachView();
//        RxJavaPlugins.reset();
    }

    @Test
    public void loadRepositoriesCallsShowRepositories() {
        String username = "ivacf";
        List<Repository> repositories = MockModelFabric.newListOfRepositories(10);
        when(githubService.publicRepositories(username))
                .thenReturn(Observable.just(repositories));

        mainPresenter.loadRepositories(username);
        verify(mainMvpView).showProgressIndicator();
        verify(mainMvpView).showRepositories(repositories);
    }

    @Test
    public void loadRepositoriesCallsShowMessage_withEmptyReposString() {
        String username = "ivacf";
        when(githubService.publicRepositories(username))
                .thenReturn(Observable.just(Collections.<Repository>emptyList()));

        mainPresenter.loadRepositories(username);
        verify(mainMvpView).showProgressIndicator();
        verify(mainMvpView).showMessage(R.string.text_empty_repos);
    }

    @Test
    public void loadRepositoriesCallsShowMessage_withDefaultErrorString() {
        String username = "ivacf";
        when(githubService.publicRepositories(username))
                .thenReturn(Observable.<List<Repository>>error(new RuntimeException("error")));

        mainPresenter.loadRepositories(username);
        verify(mainMvpView).showProgressIndicator();
        verify(mainMvpView).showMessage(R.string.error_loading_repos);
    }

    @Test
    public void loadRepositoriesCallsShowMessage_withUsernameNotFoundString() {
        String username = "ivacf";
        HttpException mockHttpException =
                new HttpException(Response.error(404, mock(ResponseBody.class)));
        when(githubService.publicRepositories(username))
                .thenReturn(Observable.<List<Repository>>error(mockHttpException));

        mainPresenter.loadRepositories(username);
        verify(mainMvpView).showProgressIndicator();
        verify(mainMvpView).showMessage(R.string.error_username_not_found);
    }
}
