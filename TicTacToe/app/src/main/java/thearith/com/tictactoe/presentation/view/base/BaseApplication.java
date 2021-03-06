package thearith.com.tictactoe.presentation.view.base;

import android.app.Application;

import com.squareup.leakcanary.BuildConfig;
import com.squareup.leakcanary.LeakCanary;

import thearith.com.tictactoe.presentation.internal.di.components.ApplicationComponent;
import thearith.com.tictactoe.presentation.internal.di.components.DaggerApplicationComponent;
import thearith.com.tictactoe.presentation.internal.di.modules.ApplicationModule;

/**
 * BaseApplication.java
 *
 * A base application class that controls life cycle of the whole application
 */

public class BaseApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initDependencies();
        initLeakCanary();
    }


    /**
     * Initializes Leak Canary (for monitoring leak memory)
     * */
    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        if(BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }


    /**
     * Initializes Dagger 2 dependencies
     * */
    private void initDependencies() {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }


    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }
}
