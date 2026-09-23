# Intellij and Java basics

Notes

## Intellij

- IntelliJ is an all-in-one dev environment for Java and Spring
- JDKs added by IntelliJ go into a shared folder (`~/.jdks`)
    - If you install a JDK yourself, it will be in a different folder. Stick to the one from IntelliJ — less chance of conflicts.
- Build systems: Maven and Gradle
    - Both manage dependencies and automate builds.
    - Maven: XML-based, simple structure.
    - Gradle: Uses Groovy/Kotlin DSL, more flexible, required for Kotlin projects.
    - Gradle is faster after the first build (thanks to caching), but slower on the first run.
- When creating a new project:
    - Gradle distribution: --Wrapper-- (recommended — the other option requires Gradle to be installed separately).
    - Ensures each project uses the Gradle version it was built with.
    - Auto-select Gradle version: Lets IntelliJ pick the latest compatible version.
    - GroupId: Like a reverse domain (e.g. `no.noroff.accelerate`) — identifies your organization or project namespace.
    - ArtifactId: The project name (e.g. `ZombieApocalypseDean`) — forms part of the final package name.
    - Tick --“Add sample code”-- to ensure IntelliJ creates a basic `Main.java` and prevents the folder structure from being empty (important for Git).
- IntelliJ now creates the standard folder structure by default (`src/main/java`, `src/test/java`, etc.)
    - These are automatically marked as --Sources Root-- and --Test Sources Root-- (blue and green icons).
    - If you manually add folders later, you may need to mark them yourself.
- Git doesn’t track empty folders, so if you skip "Add sample code," key folders might go missing when pushed.
    - You can add a `.gitkeep` file inside empty folders to keep them in version control.
- Blue folders in IntelliJ mean the folder is marked as a Sources Root — IntelliJ will compile Java files from here.
    - Packages (like org.example) won’t show until you add a class inside them — empty packages are hidden by default.
- Package names should always be lowercase with dot separators (e.g. no.noroff.zombiegame) — no camelCase or underscores.

## Naming

- Only classes and interfaces use PascalCase — everything else (methods, variables, parameters) uses camelCase.

## Testing

- Test classes don’t need accessors (public/private) — they can use default/package-private visibility.

## Equality generator

### You’re generating `equals()` for your class.

IntelliJ is asking: **how strict do you want to be when comparing two objects?**

---

### Option 1: `instanceof` (the relaxed, more flexible one)

This says:

> “If the other object is the same class *or a subclass*, and the fields match, they’re equal.”

✅ Good if you’re using inheritance (like `Tool extends Item`)
✅ Lets `Tool` and other subtypes of `Item` still compare as equals
✅ This is how most Java code is written — it’s the common choice

---

### Option 2: `getClass()` (strict, no funny business)

This says:

> “Only objects of *exactly* the same class can be equal — not subclasses.”

❌ `Tool` and another subclass of `Item` would **never** be equal
✅ Good if your class won’t be extended and you want to be extra precise
❌ Often too strict in real-world code using inheritance

---

### TL;DR:

* Choose **`instanceof`** if you're working with inheritance (like `Tool extends Item`) — it's flexible and safe for most cases.
* Choose **`getClass()`** only if you're sure you don't want subclasses to ever be considered equal — it's strict and rare.


> “I'm writing `equals()` for `Tool`, but I also want it to compare the fields from its parent class `Item` (like `name` and `weight`). How do I include those?”

---

### ✅ Simple: call `super.equals(o)` inside your `Tool.equals()`

Here’s how:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tool)) return false;
    if (!super.equals(o)) return false; // 👈 This compares the fields from Item

    Tool tool = (Tool) o;
    return Double.compare(tool.durability, durability) == 0;
}
```

This makes sure:

1. The object is a `Tool` (or subclass, if using `instanceof`)
2. The parent class `Item.equals()` returns true (i.e. same `name`, `weight`, etc.)
3. Then it compares the child-specific field, `durability`.

---

### 🔄 Don’t forget: override `equals()` in the parent too!

In `Item`, add:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Item)) return false;

    Item item = (Item) o;
    return Objects.equals(name, item.name) &&
           Objects.equals(weight, item.weight);
}

@Override
public int hashCode() {
    return Objects.hash(name, weight);
}
```

---

### 🧠 Bonus Tip: IntelliJ will *not* do this automatically

When generating `equals()` in IntelliJ, it **doesn’t include superclass fields** unless you manually add `super.equals(o)` yourself.



