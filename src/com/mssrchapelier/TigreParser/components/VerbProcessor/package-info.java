/**
 * <p>
 * Contains the {@link com.mssrchapelier.TigreParser.components.VerbProcessor.VerbProcessor VerbProcessor} class, the application's component processing <em>finite</em> verb forms,
 * and the subpackages that it relies on:
 * <ul>
 * <li>{@link com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm Paradigm}, whose class {@link com.mssrchapelier.TigreParser.components.VerbProcessor.Paradigm.Paradigm Paradigm}
 * reads finite verb paradigms from the paradigm configuration file and stores its internal representation as a mapping (namely a {@code LinkedHashMap}) of unique
 * {@link com.mssrchapelier.components.VerbProcessor.StemDescription StemDescription} objects (see below) to {@code ArrayList}s of
 * {@link com.mssrchapelier.TigreParser.components.VerbProcessor.ParadigmCell ParadigmCell} objects;</li>
 * <li>{@link com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription StemDescription}, with
 * {@link com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.StemDescription StemDescription} instances representing a combination of the number of consonants in the root
 * ({@link com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.NumRadicals NumRadicals}), the root type per Raz&nbsp;1983<a href="#footnote-1"><sup>1</sup></a>
 * ({@link com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.RootType RootType}) and the stem's derivational prefix
 * ({@link com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription.DerivPrefix DerivPrefix}; <em>verb preformative</em> in Raz&nbsp;1983);</li>
 * <li>{@link com.mssrchapelier.TigreParser.components.VerbProcessor.Stem Stem}, storing representations of actual verb roots (the {@link com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.Root Root} class)
 * and stems (the {@link com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.Stem Stem} class); also contains {@link com.mssrchapelier.TigreParser.components.VerbProcessor.Stem.RootListGenerator RootListGenerator}
 * which generates a list of possible verb roots given a (transliterated and geminated) {@code String}.</li>
 * </ul>
 * </p>
 * 
 * <p id="footnote-1"><b>[1]</b> Raz, S. <em>Tigre Grammar and Texts.</em> Malibu, California, USA: Undena Publications, 1983.</p>
 */

package com.mssrchapelier.TigreParser.components.VerbProcessor;