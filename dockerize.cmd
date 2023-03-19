REM docker build -t tracabgen5spliter:0.0.1 -t tracabgen5spliter:latest .
docker rmi tracabgen5spliter:latest -f
docker build -t tracabgen5spliter:latest .

REM if it runs on localhost
REM docker run -it -p 8080:8080 tracabgen5spliter:latest
REM if it runs on 192.168.1.100
REM docker run --env DATAPLATFORM_IP=192.168.1.100 -it -p 8080:8080 tracabgen5spliter:latest