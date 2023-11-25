# croco-back

## APIs

| Request | METHOD     |  URI | Description        |
| :-------- | :------- | :----- |:-------------------|
| `POST` | `addMatch` | `http://localhost:3000/v1/api/matches` | Add match          |
| `PUT` | `updateMatch` | `http://localhost:3000/v1/api/matches` | Update match       |
| `DELETE` | `getAllMatches` | `http://localhost:3000/v1/api/matches` | Get all matches    |
| `GET` | `getMatchById` | `http://localhost:3000/v1/api/matches` | Get match by Id    |
| `GET` | `deleteMatchById` | `http://localhost:3000/v1/api/matches` | Delete match by Id |

# Basics Java 8

## Default Methods for Interfaces

Java 8 enables us to add non-abstract method implementations to interfaces by utilizing the `default` keyword. This feature is also known as [virtual extension methods](http://stackoverflow.com/a/24102730). 

Here is our first example:

```java
interface Formula {
    double calculate(int a);

    default double sqrt(int a) {
        return Math.sqrt(a);
    }
}
```

Besides the abstract method `calculate` the interface `Formula` also defines the default method `sqrt`. Concrete classes only have to implement the abstract method `calculate`. The default method `sqrt` can be used out of the box.

```java
Formula formula = new Formula() {
    @Override
    public double calculate(int a) {
        return sqrt(a * 100);
    }
};

formula.calculate(100);     // 100.0
formula.sqrt(16);           // 4.0
```

The formula is implemented as an anonymous object. The code is quite verbose: 6 lines of code for such a simple calculation of `sqrt(a * 100)`. As we'll see in the next section, there's a much nicer way of implementing single method objects in Java 8.


## Lambda expressions

Let's start with a simple example of how to sort a list of strings in prior versions of Java:

```java
List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");

Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return b.compareTo(a);
    }
});
```

The static utility method `Collections.sort` accepts a list and a comparator in order to sort the elements of the given list. You often find yourself creating anonymous comparators and pass them to the sort method.

Instead of creating anonymous objects all day long, Java 8 comes with a much shorter syntax, **lambda expressions**:

```java
Collections.sort(names, (String a, String b) -> {
    return b.compareTo(a);
});
```

As you can see the code is much shorter and easier to read. But it gets even shorter:

```java
Collections.sort(names, (String a, String b) -> b.compareTo(a));
```

For one line method bodies you can skip both the braces `{}` and the `return` keyword. But it gets even shorter:

```java
names.sort((a, b) -> b.compareTo(a));
```

List now has a `sort` method. Also the java compiler is aware of the parameter types so you can skip them as well. Let's dive deeper into how lambda expressions can be used in the wild.


## Functional Interfaces

How does lambda expressions fit into Java's type system? Each lambda corresponds to a given type, specified by an interface. A so called _functional interface_ must contain **exactly one abstract method** declaration. Each lambda expression of that type will be matched to this abstract method. Since default methods are not abstract you're free to add default methods to your functional interface.

We can use arbitrary interfaces as lambda expressions as long as the interface only contains one abstract method. To ensure that your interface meet the requirements, you should add the `@FunctionalInterface` annotation. The compiler is aware of this annotation and throws a compiler error as soon as you try to add a second abstract method declaration to the interface.

Example:

```java
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}
```

```java
Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
Integer converted = converter.convert("123");
System.out.println(converted);    // 123
```

Keep in mind that the code is also valid if the `@FunctionalInterface` annotation would be omitted.


## Method and Constructor References

The above example code can be further simplified by utilizing static method references:

```java
Converter<String, Integer> converter = Integer::valueOf;
Integer converted = converter.convert("123");
System.out.println(converted);   // 123
```

Java 8 enables you to pass references of methods or constructors via the `::` keyword. The above example shows how to reference a static method. But we can also reference object methods:

```java
class Something {
    String startsWith(String s) {
        return String.valueOf(s.charAt(0));
    }
}
```

```java
Something something = new Something();
Converter<String, String> converter = something::startsWith;
String converted = converter.convert("Java");
System.out.println(converted);    // "J"
```

Let's see how the `::` keyword works for constructors. First we define an example class with different constructors:

```java
class Person {
    String firstName;
    String lastName;

    Person() {}

    Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
```

Next we specify a person factory interface to be used for creating new persons:

```java
interface PersonFactory<P extends Person> {
    P create(String firstName, String lastName);
}
```

Instead of implementing the factory manually, we glue everything together via constructor references:

```java
PersonFactory<Person> personFactory = Person::new;
Person person = personFactory.create("Peter", "Parker");
```

We create a reference to the Person constructor via `Person::new`. The Java compiler automatically chooses the right constructor by matching the signature of `PersonFactory.create`.

## Lambda Scopes

Accessing outer scope variables from lambda expressions is very similar to anonymous objects. You can access final variables from the local outer scope as well as instance fields and static variables.

### Accessing local variables

We can read final local variables from the outer scope of lambda expressions:

```java
final int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);

stringConverter.convert(2);     // 3
```

But different to anonymous objects the variable `num` does not have to be declared final. This code is also valid:

```java
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);

stringConverter.convert(2);     // 3
```

However `num` must be implicitly final for the code to compile. The following code does **not** compile:

```java
int num = 1;
Converter<Integer, String> stringConverter =
        (from) -> String.valueOf(from + num);
num = 3;
```

Writing to `num` from within the lambda expression is also prohibited.

### Accessing fields and static variables

In contrast to local variables, we have both read and write access to instance fields and static variables from within lambda expressions. This behaviour is well known from anonymous objects.

```java
class Lambda4 {
    static int outerStaticNum;
    int outerNum;

    void testScopes() {
        Converter<Integer, String> stringConverter1 = (from) -> {
            outerNum = 23;
            return String.valueOf(from);
        };

        Converter<Integer, String> stringConverter2 = (from) -> {
            outerStaticNum = 72;
            return String.valueOf(from);
        };
    }
}
```

### Accessing Default Interface Methods

Remember the formula example from the first section? Interface `Formula` defines a default method `sqrt` which can be accessed from each formula instance including anonymous objects. This does not work with lambda expressions.

Default methods **cannot** be accessed from within lambda expressions. The following code does not compile:

```java
Formula formula = (a) -> sqrt(a * 100);
```


## Optionals

Optionals are not functional interfaces, but nifty utilities to prevent `NullPointerException`. It's an important concept for the next section, so let's have a quick look at how Optionals work.

Optional is a simple container for a value which may be null or non-null. Think of a method which may return a non-null result but sometimes return nothing. Instead of returning `null` you return an `Optional` in Java 8.

```java
Optional<String> optional = Optional.of("bam");

optional.isPresent();           // true
optional.get();                 // "bam"
optional.orElse("fallback");    // "bam"

optional.ifPresent((s) -> System.out.println(s.charAt(0)));     // "b"
```

## Streams

A `java.util.Stream` represents a sequence of elements on which one or more operations can be performed. Stream operations are either _intermediate_ or _terminal_. While terminal operations return a result of a certain type, intermediate operations return the stream itself so you can chain multiple method calls in a row. Streams are created on a source, e.g. a `java.util.Collection` like lists or sets (maps are not supported). Stream operations can either be executed sequentially or parallely.

> Streams are extremely powerful, so I wrote a separate [Java 8 Streams Tutorial](http://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/). **You should also check out [Sequency](https://github.com/winterbe/sequency) as a similiar library for the web.**

Let's first look how sequential streams work. First we create a sample source in form of a list of strings:

```java
List<String> stringCollection = new ArrayList<>();
stringCollection.add("ddd2");
stringCollection.add("aaa2");
stringCollection.add("bbb1");
stringCollection.add("aaa1");
stringCollection.add("bbb3");
stringCollection.add("ccc");
stringCollection.add("bbb2");
stringCollection.add("ddd1");
```

Collections in Java 8 are extended so you can simply create streams either by calling `Collection.stream()` or `Collection.parallelStream()`. The following sections explain the most common stream operations.

### Filter

Filter accepts a predicate to filter all elements of the stream. This operation is _intermediate_ which enables us to call another stream operation (`forEach`) on the result. ForEach accepts a consumer to be executed for each element in the filtered stream. ForEach is a terminal operation. It's `void`, so we cannot call another stream operation.

```java
stringCollection
    .stream()
    .filter((s) -> s.startsWith("a"))
    .forEach(System.out::println);

// "aaa2", "aaa1"
```

### Sorted

Sorted is an _intermediate_ operation which returns a sorted view of the stream. The elements are sorted in natural order unless you pass a custom `Comparator`.

```java
stringCollection
    .stream()
    .sorted()
    .filter((s) -> s.startsWith("a"))
    .forEach(System.out::println);

// "aaa1", "aaa2"
```

Keep in mind that `sorted` does only create a sorted view of the stream without manipulating the ordering of the backed collection. The ordering of `stringCollection` is untouched:

```java
System.out.println(stringCollection);
// ddd2, aaa2, bbb1, aaa1, bbb3, ccc, bbb2, ddd1
```

### Map

The _intermediate_ operation `map` converts each element into another object via the given function. The following example converts each string into an upper-cased string. But you can also use `map` to transform each object into another type. The generic type of the resulting stream depends on the generic type of the function you pass to `map`.

```java
stringCollection
    .stream()
    .map(String::toUpperCase)
    .sorted((a, b) -> b.compareTo(a))
    .forEach(System.out::println);

// "DDD2", "DDD1", "CCC", "BBB3", "BBB2", "AAA2", "AAA1"
```

### Match

Various matching operations can be used to check whether a certain predicate matches the stream. All of those operations are _terminal_ and return a boolean result.

```java
boolean anyStartsWithA =
    stringCollection
        .stream()
        .anyMatch((s) -> s.startsWith("a"));

System.out.println(anyStartsWithA);      // true

boolean allStartsWithA =
    stringCollection
        .stream()
        .allMatch((s) -> s.startsWith("a"));

System.out.println(allStartsWithA);      // false

boolean noneStartsWithZ =
    stringCollection
        .stream()
        .noneMatch((s) -> s.startsWith("z"));

System.out.println(noneStartsWithZ);      // true
```

#### Count

Count is a _terminal_ operation returning the number of elements in the stream as a `long`.

```java
long startsWithB =
    stringCollection
        .stream()
        .filter((s) -> s.startsWith("b"))
        .count();

System.out.println(startsWithB);    // 3
```


### Reduce

This _terminal_ operation performs a reduction on the elements of the stream with the given function. The result is an `Optional` holding the reduced value.

```java
Optional<String> reduced =
    stringCollection
        .stream()
        .sorted()
        .reduce((s1, s2) -> s1 + "#" + s2);

reduced.ifPresent(System.out::println);
// "aaa1#aaa2#bbb1#bbb2#bbb3#ccc#ddd1#ddd2"
```

## Maps

As already mentioned maps do not directly support streams. There's no `stream()` method available on the `Map` interface itself, however you can create specialized streams upon the keys, values or entries of a map via `map.keySet().stream()`, `map.values().stream()` and `map.entrySet().stream()`. 

Furthermore maps support various new and useful methods for doing common tasks.

```java
Map<Integer, String> map = new HashMap<>();

for (int i = 0; i < 10; i++) {
    map.putIfAbsent(i, "val" + i);
}

map.forEach((id, val) -> System.out.println(val));
```

The above code should be self-explaining: `putIfAbsent` prevents us from writing additional if null checks; `forEach` accepts a consumer to perform operations for each value of the map.

This example shows how to compute code on the map by utilizing functions:

```java
map.computeIfPresent(3, (num, val) -> val + num);
map.get(3);             // val33

map.computeIfPresent(9, (num, val) -> null);
map.containsKey(9);     // false

map.computeIfAbsent(23, num -> "val" + num);
map.containsKey(23);    // true

map.computeIfAbsent(3, num -> "bam");
map.get(3);             // val33
```

Next, we learn how to remove entries for a given key, only if it's currently mapped to a given value:

```java
map.remove(3, "val3");
map.get(3);             // val33

map.remove(3, "val33");
map.get(3);             // null
```

Another helpful method:

```java
map.getOrDefault(42, "not found");  // not found
```

Merging entries of a map is quite easy:

```java
map.merge(9, "val9", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9

map.merge(9, "concat", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9concat
```

Merge either put the key/value into the map if no entry for the key exists, or the merging function will be called to change the existing value.

## Annotations

Annotations in Java 8 are repeatable. Let's dive directly into an example to figure that out.

First, we define a wrapper annotation which holds an array of the actual annotations:

```java
@interface Hints {
    Hint[] value();
}

@Repeatable(Hints.class)
@interface Hint {
    String value();
}
```
Java 8 enables us to use multiple annotations of the same type by declaring the annotation `@Repeatable`.

### Variant 1: Using the container annotation (old school)

```java
@Hints({@Hint("hint1"), @Hint("hint2")})
class Person {}
```

### Variant 2: Using repeatable annotations (new school)

```java
@Hint("hint1")
@Hint("hint2")
class Person {}
```

Using variant 2 the java compiler implicitly sets up the `@Hints` annotation under the hood. That's important for reading annotation information via reflection.

```java
Hint hint = Person.class.getAnnotation(Hint.class);
System.out.println(hint);                   // null

Hints hints1 = Person.class.getAnnotation(Hints.class);
System.out.println(hints1.value().length);  // 2

Hint[] hints2 = Person.class.getAnnotationsByType(Hint.class);
System.out.println(hints2.length);          // 2
```

Although we never declared the `@Hints` annotation on the `Person` class, it's still readable via `getAnnotation(Hints.class)`. However, the more convenient method is `getAnnotationsByType` which grants direct access to all annotated `@Hint` annotations.


Furthermore the usage of annotations in Java 8 is expanded to two new targets:

```java
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@interface MyAnnotation {}
```

# Object-Oriented-Programming-in-Java

**JAVA IS A PLATFORM-INDEPENDENT LANGUAGE**

The codes written in Java are converted into an intermediate level language called **bytecode**,after compilation which becomes a part of the java platform,independent of the machine on which the code runs.This makes Java highly portable as its bytecodes can be run on any machine by an interpreter called **Java Virtual Machine(JVM)**

**JAVA IDENTIFIERS**

An identifier can be a method name,class name,variable name or label.

**JAVA RESERVED WORDS**

There are **53** reserved words in Java of which **50** are **keywords** and **3** are **literals**(**true,false,null**)

# CLASS:

A class is a user defined blueprint or prototype from which objects are created.  It represents the set of properties or methods that are common to all objects of one type.

# OBJECT:

An object is an instance of a class.Technically, Class is a template which describes what state and behavior of an instance this class can have. Object implements the state and behavior in the form of variables and methods and requires some memory allocated.An object consists of:

    State : It is represented by attributes of an object. It also reflects the properties of an object.
    
    Behavior : It is represented by methods of an object. It also reflects the response of an object with other objects.
    
    Identity : It gives a unique name to an object and enables one object to interact with other objects.


**Declaring Objects (Also called instantiating a class):**

When an object of a class is created, the class is said to be **instantiated**. All the instances share the attributes and the behavior of the class. But the values of those attributes, i.e. the state are unique for each object. A single class may have any number of instances.

# METHODS:

A method is a collection of statements that perform certain tasks upon calling and return the result to the caller. A method can perform certain tasks without even returning anything. Methods allow us to reuse the code without retyping the code.

**Method Declaration:**
Method delaration contains six components:

1.**Modifier:** Defines access type of the method i.e. from where it can be accessed in your application. In Java, there 4 type of the access specifiers.

        public: accessible in all class in your application.
        protected: accessible within the class in which it is defined and in its subclass(es)
        private: accessible only within the class in which it is defined.
        default (declared/defined without using any modifier) : accessible within same class and package within which its class is defined.
2.**The return type :** The data type of the value returned by the method or void if does not return a value.

3.**Method Name :** the rules for field names apply to method names as well, but the convention is a little different.

4.**Parameter list :** Comma separated list of the input parameters are defined, preceded with their data type, within the enclosed parenthesis. If there are no parameters,empty parentheses () are used.

5.**Exception list :** The exceptions the method can throw can be specified.

6.**Method body :** It is enclosed between braces {} and contains the code to be executed to perform the intended operations.

**Memory allocation for methods calls:**

Methods calls are implemented through stack. Whenever a method is called a stack frame is created within the stack area and after that the arguments passed to and the local variables and value to be returned by this called method are stored in this stack frame and when execution of the called method is finished, the allocated stack frame would be deleted. There is a stack pointer register that tracks the top of the stack which is adjusted accordingly.

**Java is strictly pass by value**:

When we pass a primitive type to a method, it is passed by value. But when we pass an object to a method, the situation changes dramatically, because objects are passed by what is effectively call-by-reference. Java does what’s sort of a hybrid between pass-by-value and pass-by-reference. Basically, a parameter cannot be changed by the function, but the function can ask the parameter to change itself via calling some method within it.

While creating a variable of a class type, we only create a reference to an object. Thus, when we pass this reference to a method, the parameter that receives it will refer to the same object as that referred to by the argument.This effectively means that objects act as if they are passed to methods by use of call-by-reference.Changes to the object inside the method do reflect in the object used as an argument.

### METHOD OVERLOADING:

In java,it is possible to define two or more methods within the same class that share the same name as long as their parameter declaration are different either in terms of number of parameters or type of the parameters or both.This process is called Method Overloading.
Priority wise, compiler take these steps when the exact Prototype doesnot match with the arguments:


1.Type Conversion but to higher type(in terms of range) in same family.
2.Type conversion to next higher family(suppose if there is no long data type available for an int data type, then it will search for the float data type).

We can overload two or more static methods with same name, but differences in input parameters.We cannot overload two methods in Java if they differ only by static keyword (number of parameters and types of parameters is same).

The compiler does not consider the return type while differentiating the overloaded method. But two methods with the same signature and different return type can not be declared. It will throw a compile time error.

Hence Method overloading can be done by changing:

    1.The number of parameters in two methods.
    2.The data types of the parameters of methods.
    3.The Order of the parameters of methods.

There are 3 phases used in overload resolution: First phase performs overload resolution without permitting boxing or unboxing conversion, Second phase performs overload resolution while allowing boxing and unboxing and Third phase allows overloading to be combined with variable arity methods, boxing, and unboxing. If no applicable method is found during these phases, then ambiguity occurs.

### METHOD OVERRIDING:

When a method in a subclass has the same name, same parameters declaration and same return type(or sub-type) as a method in its super-class, then the method in the subclass is said to override the method in the super-class.

Private methods cannot be overridden as they are bonded during compile time. Therefore we can’t even override private methods in a subclass.

It is possible to have different return type for a overriding method in child class, but child’s return type must be sub-type of parent class’ return type. This is known as covariant return type.

### DYNAMIC METHOD DISPATCH/RUN-TIME POLYMORPHISM:

Method Overriding is one of the ways in which Java supports Run-time polymorphism.Dynamic Method Dispatch is a process in which a call to a overriden method is resolved during runtime rather than compile time.

1.When an overriden method is called by superclass reference,Java determines which version of the method is to be executed depending on the type of object being reffered to at the time of calling.Thus this determination is made during run-time.

2.At run-time,it depends on the type of the object being reffered to which determines the version of the overriden method to be executed.

3.A superclass reference variable reffering to a subclass object is known as upcasting.

In Java, we can override methods only, not the variables(data members), so runtime polymorphism cannot be achieved by data members.

The **java.lang.System.exit()** method exits current program by terminating running Java virtual machine. This method takes a status code.

             exit(0) : Generally used to indicate successful termination.
             exit(1) or exit(-1) or any other non-zero value – Generally indicates unsuccessful termination. 


# CONSTRUCTORS:

Constructors are used for initializing new objects. Constructors does not return any values but implicitly it returns the object of the class. Fields are variables that provides the state of the class and its objects, and methods are used to implement the behavior of the class and its objects.

**Constructors cannot be Inherited:**

When a class extends another class,the child class inherits the variables ,methods and the behaviour of the super class but not the constructors.Constructors have same name as the class name. So if constructors were inherited in child class then child class would contain a parent class constructor which is against the constraint that constructor should have same name as class name.

**Default Constructors:**

Java creates a default constructor automatically if no default or parameterized constructor is created by user.The default constructor in java initializes the member data variables to default values

**Copy Constructor:**
Java supports copy constructor but doesn't create a default copy of constructor if user doesn't create one.

### CONSTRUCTOR CHAINING:

Constructor chaining is the process of calling one constructor from another constructor with respect to current object.
Constructor chaining can be done in two ways:

1.**Within same class:** It can be done using this() keyword for constructors in same class.  

2.**From base class:** by using super() keyword to call constructor from the base class. 

This process is used when we want to perform multiple tasks in a single constructor. Rather than creating a code for each task in a single constructor we create a separate constructor for each task and make their chain which makes the program more readable.

**Points to be noted:**

    1.The this() expression should always be the first line of the constructor.
    2.There should be at-least be one constructor without the this() keyword.
    3.Constructor chaining can be achieved in any order.

### PRIVATE CONSTRUCTORS AND SINGLETON CLASS:

We can provide access specifier to the constructor. If made private, then it can only be accessed inside the class.
We can use private constructors for 1. Internal Constructor Chaining 2. Singleton class design

A singleton class is a class that can have not more than a single object.
After first time, if we try to instantiate the Singleton class, the new variable also points to the first instance created. So whatever modifications we do to a variable inside the class through any instance, it affects the variable of the single instance created and is visible if we access that variable through any instance of that class.

To design a singleton class:

    1.Make constructor as private.
    2.Write a static method that has return type object of this singleton class. Here, the concept of Lazy initialization in used to write this static method.

## CONSTRUCTORS VS METHODS:

Constructors in Java can be seen as Methods in a Class. But there is a big difference be tween Constructors and Methods.

1.Constructors have only one purpose, to create an Instance of a Class. This instantiation includes memory allocation and member initialization (Optional).

By contrast, Methods cannot be used to create an Instance of a Class.

2.Constructors cannot have Non Access Modifiers while Methods can.

3.Constructors cannot have a return type(Including void) while Methods require it.

4.The Constructor name must be the same as the Class name while Methods are not restricted.

5.As per Java naming convention, Method names should be camelcase while Constructor names should start with capital letter.

### CONSTRUCTOR OVERLOADING:

In java,it is possible to define two or more constructor of the same class that obviously share the same name but their parameter declaration are different either in terms of number of parameters or type of the parameters or both.This process is called Constructor Overloading.

# ENCAPSULATION:

Encapsulation is the mechanism that binds together code and the data it manipulates.Other way to think about encapsulation is, it is a protective shield that prevents the data from being accessed by the code outside this shield.

    1.Technically in encapsulation, the variables or data of a class is hidden from any other class and can be accessed only through any member function of own class in which they are declared.
    2.As in encapsulation, the data in a class is hidden from other classes, so it is also known as data-hiding.
    3.Encapsulation can be achieved by: Declaring all the variables in the class as private and writing public methods in the class to set and get the values of variables.

#### Advantages of Encapsulation:

1.**Data Hiding:** The user will have no idea about the inner implementation of the class. It will not be visible to the user that how the class is storing values in the variables. He only knows that we are passing the values to a setter method and variables are getting initialized with that value.

2.**Increased Flexibility:** We can make the variables of the class as read-only or write-only depending on our requirement. If we wish to make the variables as read-only then we have to omit the setter methods like setName(), setAge() etc. from the above program or if we wish to make the variables as write-only then we have to omit the get methods like getName(), getAge() etc. from the above program.

3.**Reusability:** Encapsulation also improves the re-usability and the code becomes easy to change with new requirements.

4.**Testing code:** Encapsulated code is easy to test for unit testing.

# ABSTRACTION:

Data Abstraction is defined as the process of identifying only the required characteristics of an object ignoring the irrelevant details.The properties and behaviors of an object differentiate it from other objects of similar type and also help in classifying/grouping the objects.

Consider a real-life example of a man driving a car. The man only knows that pressing the accelerators will increase the speed of car or applying brakes will stop the car but he does not know about how on pressing the accelerator the speed is actually increasing, he does not know about the inner mechanism of the car or the implementation of accelerator, brakes etc in the car. This is what abstraction is.
In java, abstraction is achieved by interfaces and abstract classes. We can achieve 100% abstraction using interfaces.


    An abstract class is a class that is declared with abstract keyword.
    An abstract method is a method that is declared without an implementation.
    An abstract class may or may not have all abstract methods. Some of them can be concrete methods
    A method defined abstract must always be redefined in the subclass,thus making overriding compulsory OR either make subclass itself abstract.
    Any class that contains one or more abstract methods must also be declared with abstract keyword.
    There can be no object of an abstract class.That is, an abstract class can not be directly instantiated with the new operator.
    An abstract class can have parametrized constructors and default constructor is always present in an abstract class.

# PACKAGES:

A package is a container within which we can store multiple classes,subpackages and interfaces.A package is a container of a group of related classes where some of the classes are accessible are exposed and others are kept for internal purpose.

Packages are used for:

    1.Avoiding namespace collision ie Preventing naming conflicts.Thus there can be two classes of same name in two different packages.
    2.Searching and using classes, interfaces, enumerations and annotations becomes easier
    3.Providing controlled access: protected and default have package level access control. A protected member is accessible by classes in the same package and its subclasses. A default member (without any access specifier) is accessible by classes in the same package only.
    4.Packages can be considered as data encapsulation (or data-hiding).

**How packages work?**

If a package name is college.dept.cse, then there are three directories, college, dept and cse such that cse is present in dept and dept is present college. Also, the directory college is accessible through **CLASSPATH** variable, i.e., path of parent directory of college is present in CLASSPATH. The idea is to make sure that classes are easy to locate.We can add more classes to a created package by using package name at the top of the program and saving it in the package directory. We need a new java file to define a public class, otherwise we can add the new class to an existing .java file and recompile it.

                // import the Vector class from util package.
               import java.util.vector; 

                // import all the classes from util package
               import java.util.*; 
                // All the classes and interfaces of this package
                // will be accessible but not subpackages.
                import package.*;

                // Only mentioned class of this package will be accessible.
                import package.classname;

                // Class name is generally used when two packages have the same
                // class name. For example in below code both packages have
                // date class so using a fully qualified name to avoid conflict
                import java.util.Date;
                import my.packag.Date;

Packages can be of two types:

**Built-in Packages:**

These packages consist of a large number of classes which are a part of Java API.Some of the commonly used built-in packages are:
1) **java.lang:** Contains language support classes(e.g classed which defines primitive data types, math operations). This package is automatically imported.

2)  **java.io:** Contains classed for supporting input / output operations.

3)  **java.util:** Contains utility classes which implement data structures like Linked List, Dictionary and support ; for Date / Time operations.

4)  **java.applet:** Contains classes for creating Applets.

5)  **java.awt:** Contain classes for implementing the components for graphical user interfaces (like button , menus etc).

6)  **java.net:** Contain classes for supporting networking operations.

**Points to be noted:**

    1.Every class is part of some package.
    2.If no package is specified, the classes in the file goes into a special unnamed package (the same unnamed package for all files).
    3.All classes/interfaces in a file are part of the same package. Multiple files can specify the same package name.
    4.If package name is specified, the file must be in a subdirectory called name (i.e., the directory name must match the package name).
    5.We can access public classes in another (named) package using: package-name.class-name

**User-defined packages:**

These are the packages that are defined by the user. First we create a directory with the desired package name (name should be same as the name of the package). Then we create the desired class inside the directory with the first statement being the package names.

Static import is a feature introduced in Java programming language ( versions 5 and above ) that allows members ( fields and methods ) defined in a class as public static to be used in Java code without specifying the class in which the field is defined.

# INTERFACES:

Interface looks like a class and has variables and methods declaration like that of a class but it doesnot contain any method definition.


    1.Interfaces specify what a class must do and not how. It is the blueprint of the class.
    2.An Interface is about capabilities like a Player may be an interface and any class implementing Player must be able to (or must implement) move(). So it specifies a set of methods that the class has to implement.
    3.If a class implements an interface and does not provide method bodies for all functions specified in the interface, then class must be declared abstract.
    4.A Java library example is, Comparator Interface. If a class implements this interface, then it can be used to sort a collection.

**Use of Interface:**

1.It is used to achieve total abstraction.

2.Since java does not support multiple inheritance in case of class, but by using interface it can achieve multiple inheritance .

3.It is also used to achieve loose coupling.

4.It provides boundness to the program.

5.Abstract classes may contain non-final variables, whereas variables in interface are final, public and static.

Inheritence can be extended or inherited.

# Abstract class:
A class with a pure virtual function ie, an abstract method is termed as Abstract class.In java, however the class has to be declared with abstract keyword to make it Abstract.
### ABSTRACT CLASS VS INTERFACE:

|**PROPERTIES**|**ABSTRACT CLASS**|**INTERFACE**|
|:------------:|:----------------:|:-----------:|
|**Methods**|Abstract class can have abstract and non-abstract methods. From Java 8, it can have default and static methods also.|Interface can have only abstract methods.|
|**Variables**|An abstract class can have final,non-final,static,non-static variables.|Variables declared in a Java interface are by default final.Interface can have only static and final variables.|
|**Implementation**|Abstract class can provide the implementation of interface.|Interface can’t provide the implementation of abstract class.|
|**Inheritance vs Abstraction**|Abstract class can be extended using keyword “extends”.|A Java interface can be implemented using keyword “implements” |
|**Multiple implementation**|An abstract class can extend another Java class and implement multiple Java interfaces.|An interface can extend another Java interface only.|
|**Accessibility of Data Members**|A Java abstract class can have class members with access as private, protected, etc.|Members of a Java interface are public by default|

### NESTED INTERFACE:

Interfaces declared as member of a class or another interface are called member interface or nested interface.

# NESTED CLASSES:

When a class is defined within another class, such classes are known as nested classes.


    1.The scope of a nested class is bounded by the scope of its enclosing class. Thus in above example, class NestedClass does not exist independently of class OuterClass.
    2.A nested class has access to the members, including private members, of the class in which it is nested. However, reverse is not true i.e. the enclosing class does not have access to the members of the nested class.
    3.A nested class is also a member of its enclosing class.
    4.As a member of its enclosing class, a nested class can be declared private, public, protected, or package private(default).

Nested classes are divided into two categories:

        1.static nested class : Nested classes that are declared static are called static nested classes.A static nested class cannot refer directly to instance variables or methods defined in its enclosing class.It can use them only through an object reference.They are accessed using the enclosing class name.
        2.inner class : An inner class is a non-static nested class.Inner class has access to all members(static and non-static variables and methods, including private) of its outer class and may refer to them directly in the same way that other non-static members of the outer class do.
Inner class are of two categories:

        1.Local Inner class: When an inner class is defined inside a block generally the method of the Outer Class or sometimes a for loop or if clause then it becomes local inner class.Local inner classes cannot have any access modifiers associated with them. However, they can be marked as final or abstract. These class have access to the fields of the class enclosing it. Local inner class must be instantiated in the block they are defined in.

        2.Anonymous Inner class:It is an inner class without a name and for which only a single object is created.

**JAVA ACCESS MODIFIERS:**

|           | **PRIVATE**|**DEFAULT**|**PROTECTED**|**PUBLIC**|
|:---------:|:----------:|:---------:|:-----------:|:--------:|
|**WITHIN SAME CLASS**|YES|YES|YES|YES|
|**FROM ANY CLASS IN SAME PACKAGE**|NO|YES|YES|YES|
|**FROM ANY SUB CLASS IN SAME PACKAGE**|NO|YES|YES|YES|
|**FROM ANY NON-SUB CLASS IN SAME PACKAGE**|NO|YES|YES|YES|
|**FROM ANY SUB-CLASS FROM DIFFERENT PACKAGE**|NO|NO|YES|YES|
|**FROM ANY NON-SUB CLASS FROM DIFFERENT PACKAGE**|NO|NO|NO|YES|


# INHERITANCE:

Inheritance is an important pillar of OOP(Object Oriented Programming). It is the mechanism in java by which one class is allow to inherit the features(fields and methods) of another class.

    Super Class: The class whose features are inherited is known as super class(or a base class or a parent class).
    
    Sub Class: The class that inherits the other class is known as sub class(or a derived class, extended class, or child class). The subclass can add its own fields and methods in addition to the superclass fields and methods.
    
    Reusability: Inheritance supports the concept of “reusability”, i.e. when we want to create a new class and there is already a class that includes some of the code that we want, we can derive our new class from the existing class. By doing this, we are reusing the fields and methods of the existing class.

### Types Of Inheritence:

**1.Single Inheritence:** In single inheritance,one subclass inherit the features of one superclass.

**2.Multilevel Inheritence:**  In Multilevel Inheritance, a derived class will be inheriting a base class and as well as the derived class also act as the base class to other class.

**3.Hierarchical Inheritence:**  In Hierarchical Inheritance, one class serves as a superclass (base class) for more than one sub class.

**4.Multiple Inheritence:** In Multiple inheritance ,one class can have more than one superclass and inherit features from all parent classes.Java does not support multiple inheritance with classes. In java, we can achieve multiple inheritance only through Interfaces.
Multiple inheritance is not supported by Java using classes , handling the complexity that causes due to multiple inheritance is very complex. It creates problem during various operations like casting, constructor chaining etc

# this KEYWORD:

'this' works as a reference to the current object, whose method or constructor is being invoked. This keyword can be used to refer to any member of the current object from within an instance method or a constructor.

# super KEYWORD:

The super keyword in java is a reference variable that is used to refer parent class objects.

# EXCEPTION HANDLING:

An exception is an unwanted or unexcepted event,which occurs during the execution of a program ie. at run-time that disrupts the normal-flow of the program's execution.

### JAVA EXCEPTION HIERARCHY:

![](https://github.com/disha2sinha/Object-Oriented-Programming-in-Java/blob/master/snapshots/Exception_Hierarchy.jpg)

# MULTITHREADING:

Most programming languages do not enable programmers to specify concurrent activities.Java makes concurrency available to the programmers through APIs.The programmer specifies that applications contain thread of execution,where each thread designates a portion of a program that may execute concurrently with other threads.This capability of java is called multithreading.

**Example**:When programs download large files such as an audio file over the internet,users may not want to wait until entire lengthy file downloads before starting the playback.To solve this,we can put multiple threads to work-one thread downloads the clip and another plays it.

Java's garbage collection is also an example of multithreading.

### Thread States :Life Cycle Of A Thread:

![](https://github.com/disha2sinha/Object-Oriented-Programming-in-Java/blob/master/snapshots/Thread_life_cycle.jpg)

A thread lifecycle is divided into five different states, which a thread may go through in its lifetime. Each thread can be in one of the following five states. Let's understand each of these different states in the order in which they are mentioned below -:


    1.New State.
    2.Runnable State.
    3.Running State.
    4.Waiting or Blocked or Sleeping State.
    5.Dead State.

### New State:

A thread enters a new state when an object of Thread class is created but the start() method hasn't been called on it yet. In new state, a thread is not considered alive as it's not a thread of execution. Once the start() method is called on the thread, it leaves the new state and enters the next state but once it leaves new state, it's impossible for it to return back to new state in its lifetime.

### Runnable State:

A thread enters a runnable state when the **start()** method has been called on it. It means, that a thread is eligible to run, but it's not yet running, as the thread scheduler hasn't selected it to run. At one point of time, there could be multiple thread in a runnable state, it's always the choice of thread scheduler to decide on which thread to move to the next state from runnable state. A thread in runnable state is considered to be alive. A thread can return to a runnable state after coming back from a sleeping, waiting/blocked or running state.

### Running State:

A thread enters a running state when the thread schedular has selected it to run(out of all the threads in a thread pool). In this state, a thread starts executing the run() method and it is alive and kicking. From the running state, a thread can enter into waiting/blocked state, runnable or the final dead state.

### Waiting or Blocked or Sleeping State:

A thread enters a waiting state in three situations:

    When a thread has called wait() method on itself and it is waiting for the other thread to notify it or wake it up.
    When a thread code has called sleep() method on a thread, asking it to sleep for a duration.
    When a thread is waiting for an Input/Output resource to be free.

When a thread finds itself in any of the above mentioned three states, such events pushes the thread into a blocking/waiting or sleeping mode and the thread is no longer eligible to run. In any of these states, the thread is still considered to be alive. When thread gets out of waiting, blocking or sleeping state, it re-enters into runnable state.

### Dead State:

This is the last state in a thread's lifetime. A thread enters the dead state after it has successfully completed executing the run() method. At this situation, it is considered to be not alive and hence if you try to call start() method on a dead thread, it raises IllegalThreadStateException.

Threads can be created by using two mechanisms :
1. **Extending the Thread class**:We create a class that extends the java.lang.Thread class. This class overrides the run() method available in the Thread class. A thread begins its life inside run() method. We create an object of our new class and call start() method to start the execution of a thread. Start() invokes the run() method on the Thread object.
2. **Implementing the Runnable Interface**:We create a new class which implements java.lang.Runnable interface and override run() method. Then we instantiate a Thread object and call start() method on this object.

1. If we extend the Thread class, our class cannot extend any other class because Java doesn’t support multiple inheritance. But, if we implement the Runnable interface, our class can still extend other base classes.

2. We can achieve basic functionality of a thread by extending Thread class because it provides some inbuilt methods like yield(), interrupt() etc. that are not available in Runnable interface.

### THREAD PRIORITIES:

Every Java thread has a priority that helps the operating system determine the order in which threads are scheduled.Informally,threads with higher priority are more important to a program and should be allocated processor time before lower-priority threads.

        public static int MIN_PRIORITY: This is minimum priority that a thread can have. Value for this is 1.
        public static int NORM_PRIORITY: This is default priority of a thread if do not explicitly define it. Value for this is 5.
        public static int MAX_PRIORITY: This is maximum priority of a thread. Value for this is 10.

**public final int getPriority()**: java.lang.Thread.getPriority() method returns priority of given thread.

**public final void setPriority(int newPriority)**: java.lang.Thread.setPriority() method changes the priority of thread to the value newPriority. This method throws IllegalArgumentException if value of parameter newPriority goes beyond minimum(1) and maximum(10) limit.

**isAlive():** Returns true or false based on whether Thread is alive or not. 

### SYNCHRONIZATION:

Multi-threaded programs may often come to a situation where multiple threads try to access the same resources and finally produce erroneous and unforeseen results.

So it needs to be made sure by some synchronization method that only one thread can access the resource at a given point of time.

Java provides a way of creating threads and synchronizing their task by using synchronized blocks. Synchronized blocks in Java are marked with the synchronized keyword. A synchronized block in Java is synchronized on some object. All synchronized blocks synchronized on the same object can only have one thread executing inside them at a time. All other threads attempting to enter the synchronized block are blocked until the thread inside the synchronized block exits the block.


