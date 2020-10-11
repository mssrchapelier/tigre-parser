# TigreParser

TigreParser is a morphological analyser of the [Tigre language](https://en.wikipedia.org/wiki/Tigre_language) (an Ethiosemitic language spoken by around 1 million people, most of whom live in Eritrea), written in Java.

This analyser was first developed in 2019 as part of an undergraduate thesis at Lomonosov Moscow State University, Russia ([Department of Theoretical and Applied Linguistics](http://tipl.philol.msu.ru/), Faculty of Philology). The thesis itself, which describes the first version of the application, is available [here](misc/diplom_bak_Karpenko.pdf) (in Russian only). The architecture has since been changed to quite a drastic extent, mainly to allow for greater modularity, but the basic algorithm has stayed the same.

## How to use

### Trying out / using without customisation

1. [Download](https://www.java.com/en/download/) and install the Java Runtime Environment for your operating system.
2. Download `tigreparser-bundle.jar` and run it like this:

```
java -jar tigreparser-bundle.jar input_file
```

You can specify the desired **output file name** with the `-o` / `--output` flag (if left unspecified, the output will be written to `output_input-file-name` in the same folder as the `.jar` file):

```
java -jar tigreparser-bundle.jar input_file -o output_file
```
or
```
java -jar tigreparser-bundle.jar input_file --output output_file
```

By default, the output will include both complete (if any) and incomplete analyses; complete analyses are shown first, then the incomplete ones (the more morphemes, the closer to the top of the list). This may make for an excessive number of analyses to be put into the output file; you can set a **cutoff value** (a positive integer) with the `-n` / `--numanalyses` flag so that no more than this number of analyses will be returned for each word:
```
java -jar tigreparser-bundle.jar input_file -n 5
```
or
```
java -jar tigreparser-bundle.jar input_file --numanalyses 5
```
A reasonable value for this parameter seems to be between 3 and 5.

The flags can be combined (but none are required; the only parameter you must pass to the program is the path to the input file):

```
java -jar tigreparser-bundle.jar input_file -o output_file -n 5
```

### Using with custom configuration files

The parser consists of a few modules, two of which can be customised through the use of **configuration files**:

- ~~the transliteration map~~ - *users are asked **not** to change this, as some of the application's internal logic depends on comparing input against specific characters;*
- the **pairs of regex patterns and replacement strings**, organised in successive levels against which the unanalysed part from each previous level is compared, contained in `res/configs/patterns` (in a separate `.json` file for each level); and
- the **finite verb form paradigm**, contained in `res/configs/verb-paradigm.config`.

If you want to customise these files, download the file `tigreparser-lean.jar` and the folders `res` and `lib` and place them all in one folder. You can then change the files in the `res/configs` folder and use the application just as described above for the `bundle` version.

You can change the location of configuration files by specifying the paths to them in the master `config.json` file (in the folder `res` by default). The paths should be relative to the location of `config.json`. You can also **change the location** of `config.json` itself by passing the new path to it as an argument with the flag `-c` / `--config`:

```
java -jar tigreparser-lean.jar input_file -c new_path_to_config_json
```

Note that you can use this flag (and, consequently, specify the configuration files to be used) with any of the `.jar` versions of the application (`bundle`, `nolib` or `lean`). However, `bundle` and `nolib` contain the default version of the configuration files inside them, and you **must** call the application with the `-c` argument, otherwise those default files will be used even if a `res` folder is present next to the `.jar` file. The `lean` version does not contain the default files and always searches for them outside of the `.jar`.

Please do not change the location of the `lib` folder or delete any of the files in it when using the `lean` or `nolib` versions (the `lib` folder contains libraries that the application uses in these two cases).

## Output format

A sample output fragment looks like this:

```
Word: ኻርጅየት

xArGy-at
xArGay-SG.F

...

Word: መሳልሕ

masAlH
ms(A)lH:IMP.2.M.SG

masAlH
(mslH).PL

...

Word: አደቀቦት

>a-[daqabot]
CAUS-#

...
```
Each analysis consists of two lines: the first is a transliterated and geminated **surface representation** of the input word, the second is the **gloss line**.
- `-` is the morpheme separator.
- Glosses for non-segmentable (cumulative) morphemes are separated by `.`.
- In finite verb forms, the gloss representation of the root is separated from any other glosses by `:`.
- Also in finite verb forms, `(2)` after a root consonant stands for morphologically significant gemination of that consonant, `(A)` - for the presence of the long `[a:]` vowel phoneme in certain verb forms (these are the dictionary features of any specific root).
- The unanalysed portion of the word is enclosed in square brackets `[...]` in the surface line and represented by `#` in the gloss line.

## Java API

The parser comes with a small API that allows you to incorporate morphological analysis of Tigre words and text fragments into your Java applications.

1. Download either `tigreparser-nolib.jar` (recommended; comes with configuration files as resources inside the `.jar` file) or `tigreparser-lean.jar` (if you intend to modify the configuration files).
2. Download the `.jar` libraries from the `lib` directory.
3. If using the `lean` version: download the `res` folder and put it in the user working directory (i. e. the one that your application will be run from; or alternatively anywhere else and specify the location of `config.json` through the builder's `setConfigFilePath` method, see below).
4. Put the application's and the libraries' `.jar` files on the classpath.
5. Import `com.mssrchapelier.TigreParser.TigreParser`:

```java
import com.mssrchapelier.TigreParser.TigreParser;
```

6. Initialise the parser by calling `TigreParser.builder().build()`. You can optionally specify the maximum number of analyses and/or the path to the master configuration file by calling `.setMaxAnalyses(int)` and/or `.setConfigFilePath(String)` respectively on the builder object:

```java
TigreParser parser = TigreParser.builder()
				.setMaxAnalyses(5) // optional
				.setConfigFilePath("path/to/custom/config.json") // optional
				.build();
```

7. Three methods to process text, which you can call on the constructed `TigreParser` instance, are now available:
- To process a **single word**: `analyseWord(String)`. Returns a `String[k][2]`, where `k` is the number of analyses produced, the individual analysis being a `String[2]` with the zeroth element representing the *surface form* of the word and the first element representing the corresponding *gloss line* (both with morpheme boundaries marked as `"-"`):

```java
String inputWord = "ትፋሀመው";
String[][] analysisArray = parser.analyseWord(inputWord); // String[k][2]
for (int i = 0; i < analysisArray.length; i++) {
	String[] analysis = analysisArray[i]; // String[2]
	
	String surface = analysis[0]; // t-fAham-aw
	String gloss = analysis[1]; // PASS-f(A)hm:PRF-3.M.PL
}
```
- To tokenise and process a **text fragment**: `analyseLine(String)`. Returns a `String[n][k_i][2]`, where `n` is the number of tokens that the input `String` was split into, each element being an array of analyses for the corresponding word as described above:
```java
String[][][] wordArray = parser.analyseLine(inputLine); // String[n][k_i][2]: analyses for all n tokens
for (int i = 0; i < wordArray.length; i++) {
	String[][] analysisArray = wordArray[i]; // String[k_i][2]: analyses for the i-th token
}
```
- To process a **text file**: `processFile(String inputFile, String outputFile)`:
```java
parser.processFile("input.txt", "output.txt");
```

You can also **change the maximum number of analyses** to return for each word after initialising the `TigreParser` instance by calling `.setMaxAnalyses(int)` on it.

For more details, refer to the application's javadoc (attached [here](doc/tigreparser-javadoc.zip) as a `.zip` archive).

## Suggested improvements

Do feel absolutely free to make any changes to this application. My suggestion would be to concentrate efforts on developing a more efficient algorithm for generating geminated variants of words (the class `TigreParser.Geminator`), as the current approach to this stage of analysis (which essentially consists in searching through all possible combinations of geminable consonants with few constraints imposed) appears to be the main reason for the (admittedly!) massive overgeneration of analyses, particularly incomplete ones. The pattern files and the verb paradigm file do seem to mostly cover the contents of Raz's grammar<sup>1</sup> (which served as the primary basis for composing the rules described in these files) and are of somewhat lesser concern, but their coverage of the language's grammar also appears to still be far from ideal, and refinements are, of course, always in order.

## Licencing

This version of TigreParser is licenced under the terms of the Apache License 2.0; the main requirement is that distributors keep the licence file and preserve the copyright notice. See [the licence](./LICENSE.TXT) and [the notice](./NOTICE.TXT) for details.

[1] Raz, S. *Tigre Grammar and Texts.* Malibu, California, USA: Undena Publications, 1983.
