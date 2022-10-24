# Outline
1. Блочные элементы
 + [JVM](#JVM)





# <a name="Parag"></a>JVM

## Memory model java (depends on JVM version)

[garbage collection](https://dzone.com/articles/all-you-need-to-know-about-garbage-collection-in-j), [Memory model](https://habr.com/ru/post/510454/), [Examples](https://javadevblog.com/chto-takoe-heap-i-stack-pamyat-v-java.html)

![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-eDn5Lo0-TU/dda79a732d6d10c0ec5a2c46baaebe8d3d01994751c0da18a785b2b3df3c963cb1e1ef54e24d967694d073eed49608e8511ae549f890c2a800c50cc623e78b6208649f5654c213cc01071b127415e3ad222b7dabd05dd11a7beb021ffcc617caa0b65b7b)![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-XcV9cSBKIh/52fcd886fdfa99b3bfb58234ebf7751dd5dfae12a8d5b7860e36c5e7c8294f77f060a6f6c830ccfb3c0d577fb40b286f9131e85f53d252d4df8f153391db1ecc4d26039da4ce60826fa343439d0b8187d8e25910d7472667712b2bc803b9d40816a8c814)

Native Memory — вся доступная системная память.  
Heap (куча) — объекты и статические переменные. Содержит все объекты приложения.

-   Переменные объекта - в heap с объектом.
-   Статические переменные - в heap с классом.

Eden, S0, S1, Old Generation – Подробнее в главе “Сборка мусора” ниже.

Thread Stack — локальные переменные и методы что вызвал поток (стек).

-   локальная переменная из методов в объекте – в stack, сам объект в heap.
-   локальная переменная ссылка на объект – в stack, сам объект в heap.
-   локальные переменные примитивных типов – в stack и не видны другим потокам.

Поток не может совместно использовать примитивную локальную переменную, только передать копию. Если два потока выполняют один и тот же код, они создадут свои копии в стеках. Все данные в стеке GC roots.

Metaspace — с Java 8 заменяет permanent generation — метаданные классов . Это пространство также является общими для всех. Так как metaspace является частью native memory, то его размер зависит от платформы.

CodeCache — JIT-компилятор компилирует часто исполняемый код, преобразует его в нативный машинный код и кэширует для более быстрого выполнения.

## Компиляция

AOT-компиляция (ahead-of-time, статическая) – процесс превращения текста на языке программирования в нативный код на машинном языке. Так работают языки вроде C++.

JIT-компиляция (just-in-time, динамическая) – «умная» интерпретация. Среда выполнения анализирует исполняемый код, оптимизируя часто вызываемые участки. Таким способом программа работает значительно быстрее. Именно с JIT-компиляцией связана необходимость «прогрева» программ перед тестированием производительности.

## Garbage Collectors

Сборщик мусора отслеживает доступность каждого созданного объекта в heap и удаляет не используемые. Сборщиков мусора много, но почти все работают по схеме:

-   Mark – когда сборщик мусора находит какие куски памяти используются, а какие нет.
-   Sweep – на этом шаге удаляются объекты которые были помечены раньше.

При сборке мусора перемещаются между eden, s1, s2, old – только хедеры объектов.

Типы сборщиков мусора

-   Serial – использует один поток, останавливает всю работу приложения.
-   Parallel – использует несколько потоков, когда работает в heap.
-   CMS(deprecated since 9 version) – использует несколько потоков для сборки мусора.
-   G1
-   Z

## Class

### Class loaders

В JVM встроено как минимум три стандартных загрузчика:

-   Bootstrap – JVM реализация, загружает часть стандартных классов java.*
-   Platform – отвечает за загрузку стандартных классов Java-рантайма(до Java 9 Extension для загрузкой расширений). Гарантируется, что ему будут видны (но не факт что загружены непосредственно им) все стандартные классы Java SE и JDK.
-   System (Application) – загружает классы из classpath конкретного приложения.

Перед тем как загрузить класс, ClassLoader проверит, не может ли это сделать его родитель. Если класс уже загружен, то загрузка не потребуется.

### Загрузка класса

1.  Сначала загружается класс и цепочка его предков. Он загружается один раз.
2.  Выполняется выделение памяти и инициализация статических полей и блоки инициализации в порядке объявления.
3.  Инстанцируется сам экземпляр и цепочки наследования, с самого дальнего родителя.
4.  Выделяется память в куче для экземпляра, получается ссылка на этот экземпляр.
5.  Выполняются выделение памяти и инициализация нестатических полей и блоков инициализации в порядке объявления.
6.  Вызывается конструктор.

### Типы классов

-   Абстрактный – помеченный ключевым словом abstract. Не может иметь экземпляры, может иметь абстрактные методы(с модификатором abstract) и обычные.
-   Внутренний (inner, non-static nested) – объявленный внутри другого класса. Не может иметь статических объявлений. Имеет доступ ко всем внутренностям экземпляра внешнего класса. Если член внешнего класса foo перекрыт членом внутреннего (shadowing), обратиться к внешнему можно с помощью конструкции OuterClassname.this.foo, без перекрытия сработает просто foo. Инстанциируется только от экземпляра внешнего класса: outer.new Inner().
-   Вложенные (nested, inner static) – имеет доступ ко всем статическим членам внешнего класса. В остальном ничем не отличается от обычного класса;
-   Локальный – объявленный внутри метода. Является внутренним классом, в случае объявления в статическом методе без доступа к экземпляру внешнего класса. Не имеет модификаторов доступа.
-   Анонимный – локальный класс, объявленный без имени, непосредственно при инстанцирование, расширением другого класса или интерфейса. Анонимный может расширять только один класс или интерфейс. Не может быть абстрактным или финальным. Замена Лямбда-выражение.
-   Финальный – с модификатором final, нерасширяемый.

Внутренние и вложенные классы могут иметь несколько уровней вложенности. Модификаторы abstract и final несовместимы, но по отдельности применимы к различным внутренним классам (кроме анонимного).

### Reflection

Это средства манипуляции данными на основе знания о структуре классов этих данных, инструменты метапрограммирования.  
Использование Reflection API медленное и небезопасное. Позволяет ломать инвариантность состояний экземпляра, нарушать инкапсуляцию, и менять финальные поля.

Использовать рефлексию естественно в тестовом коде, в инструментах разработки, в фреймворках (особенно в связке с runtime-аннотациями).

Инициализация классов-конфигураций в Spring Framework. Фреймворк с помощью рефлекшна сканирует внутренности классов. Поля и методы, помеченные специальными аннотациями, воспринимаются как элементы фреймворка.

### Abstract vs Interface
| Abstract Class                                                | Interface                                                           |
|---------------------------------------------------------------|---------------------------------------------------------------------|
| Описывает состояние и поведение                               | Описывает только поведение                                          |
| Средство наследования реализации                              | Средство наследования API                                           |
| Строит иерархию наследования с отношением IS A(близкая связь) | Устанавливает контракт (классы, у которых вообще нет ничего общего) |
| extends один раз                                              | implements много                                                    |

extends несколько раз = diamond problem когда у разных parents одинаковая сигнатура методов и не понятно какой использовать в child прив вызове.

Абстрактные метод - без реализации в абстрактном классе со словом abstract.  
Методы интерфейса неявно абстрактные.

### Class Object / 11 methods

Object - superclass/parent всех классов в джава которые неявно наследуются от него.

public final native Class</?> getClass() - Возвращает класс этого экземпляра. То есть результатом вызова .getClass() переменной типа Foo может быть как Foo.class, так и .class любого из его подклассов. Компилятор страхуется от ClassCastException в рантайме подменой возвращаемого типа метода на Class</? extends Foo>.

public native int hashCode(), public boolean equals(Object obj)Collections

-   HashCode - для эффективного поиска. Default - адрес объекта (зависит от JVM).
-   Equals - для разрешения коллизий. Default - Сравнивает ссылки как ==.

Эти два метода придуманы для использования в Collections. Методы нужно переопределить чтобы эффективно использовать экземпляры как ключи в HashMap или HashSet. Они работают эффективнее, при хорошем распределение хэшей.

Контракты:

-   Нужно переопределять только 2 вместе.
-   Если объекты равны у них одинаковый hashCode, но если hashCode одинаковый объекты не всегда равны(одинаковые поля для хэш кода или предельное значение int).

Переопределение:
```Java
@Ovveride
public boolean equals(Object o){
	//reflect
	if (this == e) return true;
	// null check
	if (o == null) reurn false;
	// class type check and class casting
	if (getClass() != o.getClass()) return false;	
	RestConfig that = (RestConfig) o;
	// field check
	return connectTimeOut == that.connectTimeOut &&
		   readTimeOut == this.readTimeOut &&
		   Objects.equals(url, that.url);
}
```
-   Рефлексивность - a.equals(a)
-   Симметрия - a.equals(b), то b.equals(a)
-   Транзитивность - a.equals(b) и a.equals(c), то b.equals(c)
-   Согласованность - equals и hashCode должны возвращать одни и те же значения для одного и того же объекта при каждом последующем вызове, даже если состояние объекта изменилось. Это делает реализацию для изменяемых (mutable) объектов крайне сложной.
-   Ничто не может быть equals(null).

protected native Object clone() throws CloneNotSupportedException – реализации нет, вызов выбросит исключение. Нужно писать свою реализацию, делать при этом ее public и с маркерным интерфейсом Cloneable. По умолчанию делает «поверхностное копирование» - в объектах будут ссылки на одни и те же сущности.  
Поэтому нужно делать “Глубокие копии” где изменяемые данные с вложенной структурой тоже копируются, а не только их ссылки.  
Это диктуется соглашением, по которому клон не должен зависеть от оригинала.  
По контракту клон должен быть другим объектом (!= оригиналу) и не зависеть от оригинала.

Альтернативы (многие считают что более удобные) метода clone - конструктор копирования и паттерн factory method.

```Java
@Ovveride
protected RestConfig clone() throws CloneNotSupportedException {
	// we need to copy every reference with new instance if object is muable
	// also each internal link
	RestConfig copied = (RestConfig) super.clone();
	copied.serUrl(new CustomObject("new instance with copied value"));
}
```

public String toString() - getClass().getName() + '@' + Integer.toHexString(hashCode())

public final native void notify() MultiThreading - возобновляет выполнение потока, из которого был вызван метод wait() для того же объекта

public final native void notifyAll() MultiThreading - возобновляет выполнение всех потоков, из которых был вызван метод wait() для того же объекта. Управление передается одному из этих потоков.

public final void wait() throws InterruptedException MultiThreading - переводит вызывающий поток в состояние ожидания. В этом случае вызывающий поток освобождает монитор, который имеет доступ к ресурсу. Ожидание продолжается до тех пор, пока другой поток, который вошел в монитор, не вызовет метод.

protected void finalize() - deprecated работал при сборке мусора, ненадежен.

## Типы данных, передача в метод, ключевые слова

[Почитать](https://javarush.ru/forum/382)

## Primitive types

| Type    | byte (8 bits) | java size | range. -2(n-1) to +2(n-1)-1 |
|---------|---------------|-----------|-----------------------------|
| byte    | 1             | 4         | -128 to 127                 |
| short   | 2             | 4         | (-2 in 15) to (2 in 15 - 1) |
| int     | 4             | 4         | (-2 in 31) to (2 in 31 - 1) |
| long    | 8             | 8         | (-2 in 63) to (2 in 63 - 1) |
| float   | 4             | 4         | not important               |
| double  | 8             | 8         | not important               |
| boolean | 1 bit         | 4         | true to false               |
| char    | 2             | 2         | 0 to (2 in 16 - 1)          |

8 примитивных и 8 Wrapper + String (9 ссылочных)

Для всех классов-оберток над примитивами и String кроме Float и Double работает механизм кэширования. Кэш реализован в виде pool-a значений для каждого типа, все pool-ы хранятся в heap. Метод intern() - берет или кладет в pool.
```Java
	Float.valueOf(1) != Float.valueOf(1)
	Integer.valueOf(1) == Integer.valureOf(1)
	Long.valueOf(1L) == Long.valueOf(1L)
	Integer.valueOf(1) != new Integer(1)
	Integer.valueOf(999) == 999
	Integer.valueOf(999) == Integer.parseInt("1")
	Character.valueOf('\u007f') == (Character) ((char) 127)
```
Значения кэшируются и в других встроенных классах: BigDecimal, Currency, пустые коллекции. Кэши реализованы в коде классов.

Некоторые значения создаются на этапе инициализации класса, и переиспользуются когда объект создается не через new(например valueOf).

Передача в методы - [Почитать](https://javarush.ru/groups/posts/857-peredacha-parametrov-v-java)

Java передает параметры по значению — скопировать значение и передать копию.  
В метод передается копия ссылки. Через которую можно изменять объект.

### Модификаторы доступа и ключевые слова

Порядок: @Аннотации, доступ, static final transient volatile  
В методах: @Аннотации, доступ, abstract static final synchronized native

| Access level | Class | Package | Child | Module |
|--------------|-------|---------|-------|--------|
| private      | +     | -       | -     | -      |
| (default)    | +     | +       | -     | -      |
| protected    | +     | +       | +     | -      |
| public       | +     | +       | +     | +      |


native — реализация метода скрыта внутри JVM, нельзя использовать в своем коде.  
transient — поле будет пропущено при сериализации.[](http://)  
abstract — ["Abstract vs interface"](https://coda.io/d/_dIPpkL0eZog/_su3Al#_luK-4)  
synchronized и volatile —  [“Multithreading”](https://coda.io/d/_dIPpkL0eZog/_suzgm)

final

-   class - запрет наследования
-   method - запрет переопределения
-   field с ссылкой - ссылка будет не изменяемой, поля самого объекта можно менять.
-   field - запрет изменения после инициализации

super

-   Задать нижнюю границу generic-типа: Consumer</? super Number> см. Generics
-   Обратиться к члену класса-родителя, который перекрыт (shadowed) членами наследника или локальными переменными: int foo = super.foo
-   Вызвать в конструкторе конструктор родителя: SubClass() { super("subclass param"); }

default - В Java 8 вместе с лямбдами и стримами появилась необходимость дополнить новыми методами интерфейсы. Чтобы не ломать обратную совместимость, добавили методы с телом через слово default. Новые методы в старых интерфейсов.

static - Ключевое слово static используется для объявления вложенных классов, статических методов, полей, блоков инициализации и статических импортов.

-   Статические поля и методы – члены класса, а не экземпляра(instance), потому к ним можно обращаться через имя класса. Код статического блока или метода имеет доступ только к статическим членам. Статические поля не участвуют в сериализации.  
    Инициализаторы статических полей выполняются в неявном статическом блоке.
-   Статические методы - используют раннее связывание, то есть вызов конкретного метода разрешается на этапе компиляции, не работают перегрузка и переопределение в наследниках.
-   Статический блок инициализации - выполняется потокобезопасно, один раз после загрузки класса загрузчиком. Блоков может быть несколько, выполнятся они в порядке объявления.
-   Static import - импортирует статические члены классов в .java-файл.

### Аннотации

Retention Policy / @Retention – где останется аннотация после компиляции.

SOURCE – аннотация присутствует только в исходном коде. Два типа:

-   Маркеры - вариант документации. Примеры: @Immutable и @ThreadSafe из Hibernate.
-   Инструкции - для инструментов разработки @SuppressWarnings и @Override могут влиять на предупреждения и ошибки компиляции.

CLASS –  попадает в байткод .class-файла, но игнорируется загрузчиком классов. Недоступна для рефлекшна. Используется для инструментов, обрабатывающих байткод.

RUNTIME – для снабжения метаинформацией, доступной во время выполнения программы. Используется для чтения метаинформации класса/объекта через Reflection API. Используется во множестве популярных фреймворков: Spring, Hibernate, Jackson.

Мета-аннотации – это аннотации для объявления других аннотаций. Вообще мета-аннотациями можно назвать любую аннотацию с таргетом ANNOTATION_TYPE.

@Repeatable – применяема несколько раз к одному и тому же элементу.

@Deprecated – устаревший.

@Inherited – применяется к наследникам. @Target - определяет, в каком контексте может применяться объявляемая аннотация. Возможные таргеты:

TYPE – Объявление класса, интерфейса, аннотации или enum-а.  
FIELD – Объявление поля (включая константы enum-ов).  
METHOD – Объявление метода.  
PARAMETER – Формальный параметр в объявлении метода.  
CONSTRUCTOR – Объявление конструктора.  
LOCAL_VARIABLE – Объявление локальной переменной.  
ANNOTATION_TYPE – Объявление аннотации. Применяется для создания мета-аннотации.  
PACKAGE – Объявление пакета (в package-info.java).  
TYPE_PARAMETER – Объявление generic типа-параметра.  
TYPE_USE – Любое использование типа. Например: (@NonNull String) myObject.  
MODULE – Объявление модуля.

### Функциональные интерфейсы

Нужны для использования с lambda выражениями.

![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-U3Wx0MuFmI/3141bc506deffbebb0bedd6df1d2daf908bb261f5bd6b69293b6b2913bf2ad3deba7e88bd68669ebaf970c8b49052bc3a522cbdae8a26da1022e7192957f5612197f7c21e115e6212ed18e3f4b75188630eb841d063157091930ed3e5fb43f023f7ed55e)![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-I7zn2Fjcme/5385fe37d1539ec907c1352897828f11b9a4ca5721e737df85ccf92d85d4ca4f574161baf643a1b0d5c20e04b069d8ae98f250ad8b20c4df209baeaef51b496ecdd51a2ff07f782ab0005bcdc1bbe834ccdd8d8a66839221a811396cb3c3c47037b020f5)

Это интерфейс, который содержит ровно один абстрактный метод, то есть описание метода без тела. Статические методы и default методы при этом не в счёт. 

### Виды String

Классы String, StringBuffer и StringBuilder. Класс String final для того чтобы работал string pool, не изменяемости паролей и прочего, потокобезопасности, имена классов. А эти два вспомогательных класса реализуют для него паттерн Builder и служат способом редактирования строки без относительно дорогого пересоздания объекта.

Все методы StringBuffer синхронны. В Java 1.5 ему на замену пришел несинхронизированный вариант StringBuilder. Эта ситуация аналогична HashMap и Hashtable. В остальном эти два класса почти ничем не отличаются, имеют одинаковый набор методов и конструкторов.

![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-Bp4m4aoE20/55905d13b9705cc6a958669cd9eddb4af225da0e02424fd2e4e25597a2c71e81b20885c7d98aadd3c7f092972be3055f250fc58b6b00e4b7a565f38842481fc41b3be940008f3ba20520071f68af15cc88ff3ee10f044b9eef9f96cfd4d9a8f72cd2b844)

Для буфера и билдера не работает синтаксический сахар строк:

Их нельзя создать литералом, вместо этого используется обычный конструктор.  
Нельзя конкатенировать +, вместо этого используются обычные методы insert и append.

Сам оператор конкатенации константных выражений, компилируется в интернированную строку, но для не-констант неявно использует StringBuilder.

String a = “cat”; String b = “ca” + “t”; a == b → true.

## Exception

Иерархия исключений:

Throwable, RunTime, Error, Exception - classes.  
Error - ошибки jvm, исполняемой среды. (Нельзя обработать и восстановиться)  
Exception - ошибки которые мы можем обработать, ошибка в коде. (Можно восстановиться)

Хорошей практикой считается использовать не проверяемые исключения. Т.к. проверяемые раскрывают инкапсуляцию(были введены давно и устарели), по комментарию к методу можно легко понять что нужно ловить. Во многих языках применяются только не проверяемые исключения.  
Разница между в checked и unchecked что мы обязаны обработать checked или пробросить дальше.

finally может вернуть значение и shadowing исключение, как будто его и не было.

Interface Closable закрывает ресурс при вызове close(). Autocloseable – закрывает автоматически при try with resources.

![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-4vdJhyQ-up/c31f73264974f41915e3d5aa4be4501336389b5c944d993bc60de1fbac44948f1fcae6b271c6cf06d3b02ab6973cc35ce53b05f06cb58232cd479e66a89cd225e47dd9840c310196888ca375622afee7c83144840c569c41e0d1affde956119259362e73)

## Stream API

Это средства потоковой обработки данных в функциональном стиле. Для работы с набором данных как с одной сущностью, можно представить в виде конвейера.

Операции:

-   Source(Источником) - может быть заранее заданный набор данных, или динамический генератор, возможно даже бесконечный.
-   Intermediate(Промежуточные) - операции модифицируют стрим. Можно вызвать сколько угодно таких операций. Метод peek() – только для дебага bad practice т.к. он может вносить изменения в сам объект. Возвращает Stream.
-   Terminal(Терминальная) - операция выполняет всю цепочку и возвращает значение. Может быть только одна, в конце stream. Стрим не выполнится до вызова терминальной операции.

collect(Collector collector) - метод собирает все элементы в список, множество или другую коллекцию, группирует элементы по какому-нибудь критерию, объединяет всё в строку и т.д.:

![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-vV5vznHcsj/711e1ab608973323b40456afebc20ec857a428a4aba9c31dd0407d30f8b40d1679a5613d0344fb8d89a3ac8e82c59cd3eb39bcccb90571ac99277e3b0c9c9f9d53b8c7632c4e2400050e6e39478e9d6a991ea75906c034e2b2ee4e9606e2ccbc89bf41cc)

flatMap() - преобразует каждый элемент в Stream, объединяет Stream всех элементов в один и передает его следующему оператору.

Loop for

Stream API

Работает эффективнее в 1 поток

Работает быстрее если парралельно, но нужно грамотно испольщовать, иначе будем медленнее чем в 1 поток

Плохая читабельность если внутри много кода

Хорошая читабельность, т.к. есть методы которые описывают что они делают map, filter...Это делает код более поддерживаемым

Использовать на малом обьеме данных

При больщом обьеме данных

Parallel streams – не нужно использовать если есть лаг по сети или батчи добавляются очень долго. Иначе закончатся потоки у Executor Pool.

![image.png](https://codahosted.io/docs/IPpkL0eZog/blobs/bl-JKw3JrCBaK/aa5861ea7f2fc962db40a706e5db44c63e82c62cbf7a5497552d9bc441304e7e24a712330feea967f1e81da5e387e06eec5de87c80be5bb25b6907129fe6464ebca0e34249e01d0f007f410cc931f9ad3549674efbd809bd9f2252044a96254288608ec4)

### Параллельные стримы

Основная цель, ради которой в Java 8 добавили Stream API – удобство многопоточной обработки.

Обычный стрим будет выполняться параллельно после вызова промежуточной операции parallel(). Для распараллеливания используется единый общий ForkJoinPool. Дробит сложные операции на простейшие и так рекурсивно до тех пор пока конечная операция не станет элементарной. Выполняются на разных ядрах процессора - parallel() блокирует метод основного потока пока не вернет результат.

Если обрабатываем мало данных в параллель стримах то преимущества не будут задействованы.

При очень большом количестве элементов будет множество форк джоинов - производительность упадет еще больше чем при обычном стриме.

## Generics

Основная идея показать с какими типами данных может работать класс или метод.

-   Типы дженерики обеспечивают параметрический полиморфизм, т.е выполнение идентичного кода для различных типов. Если к разным типам можно безопасно применять одну и ту же логику.Типичный пример — коллекции, итераторы
-   type-erasure — это стирание информации о типе-параметре в runtime. Таким образом, в байт-коде мы увидим List, Set вместо List<Integer>, Set<Integer>, ну и type-cast'ы при необходимости

Обобщения или generics (обобщенные типы и методы) позволяют нам уйти от жесткого определения используемых типов.

Когда мы используем generic мы ограничиваем тип данных с которым класс или метод может работать.Не может работать с примитивами.

Можно обходиться и без generic но придется писать больше кода(реализация для каждого конкретного типа, не нужно проверять типы при class casting для защиты от ClassCastException) и менее безопасно.

</? extends Number> - Number и его наследники

</? super Number> - Number и его предки(Object в данном случае)

### Вывод типов

Type inference – это способность компилятора догадаться, какой тип нужно подставить, и сделать это за вас. Вывод происходит статически, только на основании типов аргументов и ожидаемого типа результата

## Виды ссылок

Обычная жесткая ссылка – любая переменная ссылочного типа. Очистится сборщиком мусора не раньше, чем станет неиспользуемой (перестанет быть доступной из GC roots, подробнее в следующих постах).

SoftReference – мягкая ссылка. Объект не станет причиной израсходования всей памяти – гарантированно будет удален до возникновения OutOfMemoryError. Может быть раньше, зависит от реализации сборщика мусора.

WeakReference – слабая ссылка. Слабее мягкой. Не препятствует утилизации объекта, сборщик мусора игнорирует такие ссылки.

PhantomReference – фантомная ссылка. Используется для «предсмертной» обработки объекта: объект доступен после финализации, пока не очищен сборщиком мусора.

## DateTime

В пакете java.util расположены старые классы стандартной библиотеки Java: Date Эти классы обладали рядом известных проблем. Экземпляры были изменяемыми, что делало их потоко-небезопасными.

Постепенно стандартом де-факто стала сторонняя библиотека Joda-Time.

В Java 8 был добавлен пакет java.time, который взял решения из Joda-Time в стандарт, создатель библиотеки участвовал в разработке. Ключевые классы пакета:

• LocalDate, LocalTime и LocalDateTime – локальные для пользователя дата/время.

• ZonedDateTime – дата/время в определенной часовой зоне.

• Period и Duration – периоды дат и времени соответственно.

## IO vs NIO

IO

NIO

Потокоориентированный ввод/вывод

Буфер-ориентированный ввод/вывод

Блокирующий (синхронный) ввод/вывод

Неблокирующий (асинхронный) ввод/вывод

Основное отличие между двумя подходами в организации ввода/вывода

Потокоориентированный ввод/вывод подразумевает чтение/запись из потока/в поток одного или нескольких байт в единицу времени поочередно. Данная информация нигде не кэшируются. Таким образом, невозможно произвольно двигаться по потоку данных вперед или назад(В NIO МОЖНО). Если вы хотите произвести подобные манипуляции, вам придется сначала кэшировать данные в буфере.

Потоки ввода/вывода (streams) в Java IO являются блокирующими. Это значит, что когда в потоке выполнения (tread) вызывается read() или write() метод любого класса из пакета java.io.*, происходит блокировка до тех пор, пока данные не будут считаны или записаны. Поток выполнения в данный момент не может делать ничего другого.

Неблокирующий режим Java NIO позволяет запрашивать считанные данные из канала (channel) и получать только то, что доступно на данный момент, или вообще ничего, если доступных данных пока нет. Вместо того, чтобы оставаться заблокированным пока данные не станут доступными для считывания, поток выполнения может заняться чем-то другим.

Селекторы в Java NIO позволяют одному потоку выполнения мониторить несколько каналов ввода.