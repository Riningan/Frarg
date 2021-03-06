# Frarg

[ ![Download](https://api.bintray.com/packages/riningan/AndroidMaven/Frarg/images/download.svg) ](https://bintray.com/riningan/AndroidMaven/Frarg/_latestVersion)[ ![Download](https://api.bintray.com/packages/riningan/AndroidMaven/Frarg-annotations/images/download.svg) ](https://bintray.com/riningan/AndroidMaven/Frarg-annotations/_latestVersion)[ ![Download](https://api.bintray.com/packages/riningan/AndroidMaven/Frarg-processor/images/download.svg) ](https://bintray.com/riningan/AndroidMaven/Frarg-processor/_latestVersion)

New fragment instance generator


How it works
---

This library generate final class with static *"newInstance"* methods.

For example: 

You have **FooFragment** class. *Frarg* generate static method **newFooFragmentInstance** which return fragment intance.


How to use it
---

## 1. Add dependency

```groovy
dependencies {
    implementation 'com.riningan.frarg:frarg:1.1'
    implementation 'com.riningan.frarg:frarg-annotations:1.1'
    annotationProcessor 'com.riningan.frarg:frarg-processor:1.1'
}
```

## 2. Add annotations

### Fragment without arguments

Add **@ArgumentedFragment()** annotation to fragment class.

```java
@ArgumentedFragment
public class FooFragment extends Fragment {}
```

Generated method:

```java
public static FooFragment newFooFragmentInstance() {
    FooFragment fragment = new FooFragment();
    android.os.Bundle bundle = new android.os.Bundle();
    fragment.setArguments(bundle);
    return fragment;
}
```

### Fragment with arguments

Add **@ArgumentedFragment()** annotation to fragment class.

Add **@Argument** annotation to field which will be argument.

```java
@ArgumentedFragment
public class FooFragment extends Fragment {
    @Argument
    String mArgumentString = null;
    @Argument
    int mArgumentInt = 0;
}
```

In this case *Frarg* generate argument class:

```java
public final class FooFragmentArgs {
    public String mArgumentString;
    public int mArgumentInt;
    
    public FooFragmentArgs(String mArgumentString, int mArgumentInt) {
        this.mArgumentString = mArgumentString;
        this.mArgumentInt = mArgumentInt;
    }
}
```

And two static methods:

```java
public static FooFragment newFooFragmentInstance(String mArgumentString, int mArgumentInt) {
    FooFragment fragment = new FooFragment();
    android.os.Bundle bundle = new android.os.Bundle();
    bundle.putString("mArgumentString", mArgumentString);
    bundle.putInt("mArgumentInt", mArgumentInt);
    fragment.setArguments(bundle);
    return fragment;
}

public static FooFragment newFooFragmentInstance(FooFragmentArgs args) {
    return newFooFragmentInstance(args.mArgumentString, args.mArgumentInt);
}
```

For bind arguments to fields use method:

```java
FrargBinder.bind(this);
```

Where **this** is **FooFragment** instance.

For example:

```java
@ArgumentedFragment
public class FooFragment extends Fragment {
    ...
    @Override 
    public void onAttach(Context context) {
        super.onAttach(context);
        FrargBinder.bind(this);
    }
}
```

### Fragment with arguments, but arguments declared in another class

Add **@ArgumentedFragment()** annotation to class which contain declared arguments fileds.

Add fragment class name to **fragmentCls** parameter in **@ArgumentedFragment()** annotation.

Add **@Argument** annotation to field which will be argument.

```java
@ArgumentedFragment(fragmentClass = FooFragment.class)
public class FooPresenter {
    @Argument
    ArrayList<String> mPresenterArgumentString = null;

    @Argument
    int mPresenterArgumentInt = 0;
}
```

In this case *Frarg* generate argument class and two static methods like **"Fragment with arguments"**.

For bind arguments to fields use method:

```java
FrargBinder.bind(this, bundle);
```

Where **this** is **FooPresenter** instance and **bundle** is **FooFragment::getArguments()**.

For example:

```java
public class FooFragment extends Fragment {
    FooPresenter presenter = new FooPresenter();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter.bind(getArguments());
    }
}
```

```java
@ArgumentedFragment(fragmentClass = FooFragment.class)
public class FooPresenter {
    ...
    public void bind(Bundle bundle) {
        FrargBinder.bind(this, bundle);
    }
}
```

Or call **FrargBinder.bind** in fragment class.

For example:

```java
public class FooFragment extends Fragment {
    FooPresenter presenter = new FooPresenter();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FrargBinder.bind(presenter, getArguments());
    }
}
```

### Fragment with optional arguments (new in version 1.1)

Add parameter *optional* to **@Argument** annotation.

For example:

```java
@ArgumentedFragment
public class FooFragment extends Fragment {
    @Argument(optional = true)
    String mArgumentString = null;
}
```

In this case *Frarg* generate argument class:

```java
public final class FooFragmentArgs {
  public String mArgumentString;

  public boolean mArgumentStringInitialized;

  public FooFragmentArgs(String mArgumentString) {
    this.mArgumentString = mArgumentString;
    this.mArgumentStringInitialized = true;
  }

  public FooFragmentArgs() {
  }
}
```

if argument passed to **FooFragmentArgs** constructor, it will received in **FrargBinder.bind**.

**Beware if using more than one optional argument with the same type. In this case *Frarg* generate argument class:**

```java
public final class FooFragmentArgs {
  public String mArgumentString1;

  public boolean mArgumentString1Initialized;

  public String mArgumentString2;

  public boolean mArgumentString2Initialized;

  public FooFragmentArgs(String mArgumentString1, String mArgumentString2) {
    this.mArgumentString1 = mArgumentString1;
    this.mArgumentString1Initialized = true;
    this.mArgumentString2 = mArgumentString2;
    this.mArgumentString2Initialized = true;
  }

  public FooFragmentArgs(String mArgumentString1) {
    this.mArgumentString1 = mArgumentString1;
    this.mArgumentString1Initialized = true;
  }

  public FooFragmentArgs() {
  }
}
```

Only one value will be passed as argument.

## 3. Using

### Fragment without arguments

```java
FragmentBuilder.newFooFragmentInstance();
```

### Fragment with arguments

```java
FragmentBuilder.newFooFragmentInstance("param", 101)
```

or

```java
FragmentBuilder.newFooFragmentInstance(new FooFragmentArgs("param", 101));
```


Aliases
---

If you have two fragment class with same name in different packages, you need use alias:

```kotlin
@ArgumentedFragment(alias = "FragmentNotD")
class FragmentD : Fragment() {
}
```

In this case *Frarg* generate method:

```java
public static FragmentD newFragmentNotDInstance() {
    FragmentD fragment = new FragmentD();
    android.os.Bundle bundle = new android.os.Bundle();
    fragment.setArguments(bundle);
    return fragment;
}
```


Supported argument types
---

All types which can be putting to bundle.

https://developer.android.com/reference/android/os/Bundle

And Enum classes:

```java
public enum ArgEnum {
    FIRST, SECOND, THIRD
}

@Argument
ArgEnum mArgumentEnum = null;
```


LICENCE
-----

  	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	   http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
