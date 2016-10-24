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

    // ...

    private static final int DAGGER_COMPONENT = 1;
    private static final int LIST_OF_THINGS = 2;

    @Loda.Lazy(DAGGER_COMPONENT)
    CrossConfigComponent activitySuperScopedComponent() {
        return ((MyApp) getApplication())
                .rootComponent()
                .crossConfigComponent(new CrossConfigModule());
    }

    @Loda.Got(DAGGER_COMPONENT)
    void injectMe(CrossConfigComponent component) {
        component.mainComponent(new ActivityModule(this)).inject(this);
    }

    @Loda.Async(LIST_OF_THINGS)
    List<String> simulatedFetch() throws InterruptedException {
        Thread.sleep(5000);
        return Arrays.asList("s", "t", "u", "f", "f");
    }

    @Loda.Got(LIST_OF_THINGS)
    void got(List<String> stuff, Loda.Caught interrupted) {
        try {
            interrupted.rethrow();
            for (String o : stuff) {
                view.show(o);
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

    @Inject MainPresenter presenter;
    @Inject MainView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidLoda.of(this).prepare(this);
    }

    // ...
}
~~~
Your members are now ready to use after `super.onStart()`. The component instance
will persist across configuration changes. Consumer `got()` will be called with
the list in ~5 seconds during the first run, instantly after rotation.


### License
dunno yet.
