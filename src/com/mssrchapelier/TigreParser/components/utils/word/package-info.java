/**
 * <p>Contains the internal representations of word objects and their components used throughout the application.</p>
 * 
 * <p>A {@link com.mssrchapelier.TigreParser.components.misc.word.WordEntry WordEntry} object couples an input word (a {@code String} either specified
 * by the user or an element of the array returned by {@link com.mssrchapelier.TigreParser.Tokeniser#tokenise Tokeniser.tokenise})
 * with an {@code ArrayList} of {@link com.mssrchapelier.TigreParser.components.utils.word.WordAnalysis WordAnalysis} instances, each of which represents
 * a single analysis of the input word. An analysis is construed to consist of two lines, the surface line (the transliterated version
 * of the input word; see {@link com.mssrchapelier.TigreParser.Transliterator Transliterator}) and the gloss line,
 * each segmented into the word's constituent morphemes. {@code WordAnalysis} implements separation of the word not into the surface and gloss parts,
 * but first into the morphemes, which are represented by an {@code ArrayList} of {@link com.mssrchapelier.TigreParser.components.utils.word.AnalysisSegment AnalysisSegment} objects.
 * {@code AnalysisSegment} is an abstract class representing a single segment of the word's two-line (surface and gloss) analysis; this class has as its subclasses
 * {@link com.mssrchapelier.TigreParser.components.utils.words.MorphemeAnalysis MorphemeAnalysis}, the representation of a morpheme that has been detected
 * by the {@link com.mssrchapelier.TigreParser.PatternProcessor PatternProcessor}'s or {@link com.mssrchapelier.TigreParser.components.VerbProcessor.VerbProcessor VerbProcessor}'s
 * {@code processWord} method (which has both a surface form and a gloss associated with it), and {@link com.mssrchapelier.TigreParser.components.utils.words.UnanalysedMorpheme UnanalysedMorpheme},
 * which represents the unanalysed part of the input word (and only has a surface form associated, but no gloss).
 * </p>
 * 
 * <p>
 * The package also contains the class {@link com.mssrchapelier.TigreParser.components.utils.word.WordAnalysisComparator WordAnalysisComparator}, which is used to sort a collection of {@code WordAnalysis} objects
 * when instantiating {@code WordEntry}, so that analyses deemed to be more complete (see the {@link com.mssrchapelier.TigreParser.components.utils.word.WordAnalysisComparator#compare compare} method of the class)
 * will appear in the beginning of the collection (when reverse ordering is applied; i. e. the more complete an analysis, the greater).
 * </p>
 * 
 * <p>
 * <strong>This package is intended for internal use only.</strong>
 * </p>
 */

package com.mssrchapelier.TigreParser.components.utils.word;