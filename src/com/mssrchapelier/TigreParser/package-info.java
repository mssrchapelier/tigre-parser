/**
 * <p>
 * The main package containing two public classes:
 * <ul>
 * <li>{@link com.mssrchapelier.TigreParser.TigreParser TigreParser}, the main class of the application performing morphological analysis on Tigre words, text fragments and text files;</li>
 * <li>{@link com.mssrchapelier.TigreParser.TigreParserCliLauncher TigreParserCliLauncher}, a command-line interface for {@code TigreParser}.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Processing is handled by the {@link com.mssrchapelier.TigreParser.Tokeniser Tokeniser} and {@link com.mssrchapelier.TigreParser.WordProcessor WordProcessor} classes.
 * {@code Tokeniser} is responsible for tokenising text segments into {@code String}s representing individual words (in Ethiopic script);
 * {@code WordProcessor} builds a {@link com.mssrchapelier.components.utils.word.WordEntry WordEntry} containing a list of analyses ({@link com.mssrchapelier.components.utils.word.WordAnalysis WordAnalysis} instances) for each word
 * (refer to documentation for the {@link com.mssrchapelier.TigreParser.components.utils.word word} package for detailed information about {@code WordEntry} and its components).
 * {@link com.mssrchapelier.TigreParser.ConfigBuilder ConfigBuilder} reads configuration options for the application (from the path to a {@code .json} configuration file specified as its parameter).
 * The package also contains components of {@code WordProcessor} (except {@code VerbProcessor}):
 * <ul>
 * <li>{@link com.mssrchapelier.TigreParser.Transliterator Transliterator} (along with {@link com.mssrchapelier.TigreParser.NotEthiopicScriptException NotEthiopicScriptException} which it throws that is handled by {@code WordProcessor});</li>
 * <li>{@link com.mssrchapelier.TigreParser.Geminator Geminator};</li>
 * <li>{@link com.mssrchapelier.TigreParser.PatternProcessor PatternProcessor} (along with {@link com.mssrchapelier.TigreParser.ReplaceRule ReplaceRule} whose instances it uses).</li>
 * </ul>
 * {@link com.mssrchapelier.TigreParser.components.VerbProcessor.VerbProcessor VerbProcessor}, a major component of {@code WordProcessor}, is part of the {@link com.mssrchapelier.TigreParser.components.VerbProcessor VerbProcessor} subpackage.
 * </p>
 * 
 * <p>
 * <strong>Please do note that this is the only package in this application that client code should make use of.</strong> Subpackages of {@link com.mssrchapelier.TigreParser.components components} are intended for internal use only.
 * </p>
 */

package com.mssrchapelier.TigreParser;