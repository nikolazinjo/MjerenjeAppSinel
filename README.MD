# Measurement Wizard

***
##### GENERAL DESCRIPTION
> This small desktop application is created with purpose to obtain the measurements from external device which is connected
> over USB while I was at internship at Sinel d.o.o (Labin). Measuring device sends data as plain text in ASCII format where
> every new record is delimited by the \r\n sequence of characters. Application listens on incoming data and shows them in
> table as they come. In parallel, data is also updated in excel sheet which is chosen in drop down list. After the data
> collection is completed, collected data can be saved using the option 'Write data' which will store the
> records to opened excel template.

***
##### COMPILING
> To compile with Apache Maven software project management and comprehension tool, you must open a top working directory of
> this project and run one of the following commands:
> 1) mvn clean compile assembly:single install
> 2) mvn clean install
> and executable file will be created. Note that both commands will provide two versions of .jar executable files where one
> of them is build with all included dependencies.