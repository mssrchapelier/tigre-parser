# tigre-parser

Морфологический анализатор языка тигре (входные данные -- текст в эфиопской графике).


Запуск `tigre-parser.jar`:

`javac -jar tigre-parser.jar`


Опции:
* -i "filename", -input "filename": путь к входному файлу (по умолчанию input.txt)
* -o "filename", -output "filename": путь к выходному файлу (по умолчанию output.txt)
* -numanalyses : задать максимальное число выводимых для каждого слова разборов (по умолчанию выводятся все)

В выходных файлах # в строке глосс означает, что анализатор пометил данный разбор как нефинальный.
