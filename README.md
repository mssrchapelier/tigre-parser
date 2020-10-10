# tigre-parser

Морфологический анализатор языка тигре (входные данные -- текст в эфиопской графике).


Запуск `tigre-parser.jar`:

`javac -jar tigre-parser.jar`


Опции:
* `-i "filename", -input "filename"`: путь к входному файлу (по умолчанию `input.txt`)
* `-o "filename", -output "filename"`: путь к выходному файлу (по умолчанию `output.txt`)
* `-numanalyses` : задать максимальное число выводимых для каждого слова разборов (по умолчанию выводятся все)

В выходных файлах `#` в строке глосс означает, что анализатор пометил данный разбор как нефинальный.

# TigreParser

TigreParser is a morphological analyser of the Tigre language (a Ethiosemitic language spoken by around 1 million people, most of whom live in Eritrea) written in Java.

This analyser was first developed as part of an undergraduate thesis at Lomonosov Moscow State University, Russia (Department of Theoretical and Applied Linguistics, Faculty of Philology). The thesis itself, describing the first version of the application, is available [/misc/diplom_bak_Karpenko.pdf](here) (in Russian only). The architecture has since been changed to quite a drastic extent, mainly to allow for greater modularity, although the basic algorithm has stayed the same.

(basic instructions)

(using the API)

(suggested improvements)

(other contents: build.xml, misc, ...)
