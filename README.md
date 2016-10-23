# Lod\[a\]
Do your activities choke whenever the user rotates the phone? Loda knows
that feel.


## Installation
it's not even implemented yet.


## Why
blog post?


## Usage
Starting with your plain activity:
~~~java
public class MainActivity extends AppCompatActivity {

    @Inject MainPresenter presenter;
    @Inject MainView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
~~~
Define and annotate the producer and consumer methods:
~~~java
public class MainActivity extends AppCompatActivity {

    private static final int DAGGER_COMPONENT = 1;
    private static final int LIST_OF_THINGS = 2;

    @Inject MainPresenter presenter;
    @Inject MainView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Loda.Lazy(DAGGER_COMPONENT)
    CrossConfigComponent activityScopedComponent() {
        return ((MyApp) getApplication())
                .rootComponent()
                .crossConfigComponent(new CrossConfigModule());
    }

    @Loda.Got(DAGGER_COMPONENT)
    void injectMe(CrossConfigComponent component) {
        component.mainComponent(new ActivityModule(this)).inject(this);
    }

    @Loda.Async(LIST_OF_THINGS)
    List<Object> fetch() throws InterruptedException {
        Thread.sleep(5000);
        return Arrays.asList("s", "t", "u", "f", "f");
    }

    @Loda.Got(LIST_OF_THINGS)
    void got(List<Object> stuff, Loda.Caught interrupted) {
        try {
            interrupted.rethrow();
            for (Object o : stuff) {
                // do something with stuff
            }
        } catch (Exception e) {
            Toast.makeToast(e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
~~~
Rebuild your project to compile the loader builder class. Then instantiate and
use it in `onCreate()`:
~~~java
public class MainActivity extends AppCompatActivity {

    private static final int DAGGER_COMPONENT = 1;

    @Inject MainPresenter presenter;
    @Inject MainView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MainActivityLoda(this).prepare(this);
    }

    // ...
}
~~~
Your members are now ready to use after `onStart()`. The component instance
will persist across configuration changes. Consumer `got()` will be called with
the list in ~5 seconds during the first run, instantly after rotation.


### License
dunno yet.
