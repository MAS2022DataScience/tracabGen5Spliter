docker build -t tracabGen5Spliter:0.0.1 -t tracabGen5Spliter:latest .

REM if it runs on localhost
REM docker run -it -p 8080:8080 tracabGen5Spliter:latest
REM if it runs on 192.168.1.100
REM docker run --env DATAPLATFORM_IP=192.168.1.100 -it -p 8080:8080 tracabGen5Spliter:latest