rem ‘айл make_console.bat
rem компил€ци€ и компоновка консольных приложений
rem в Borland Builder C++ 5.5
rem объ€вление переменных
path c:\temp\BCC55\bin;%path%
set include=c:\temp\BCC55\include
set lib=c:\temp\BCC55\lib
rem удал€ем прежние результаты компил€ции
if exist %appp%.exe del %app%.exe
if exist %appp%.obj del %app%.obj
rem запуск компил€тора
bcc32.exe -I%include% -L%lib% level9.c generic.c > errout.txt
