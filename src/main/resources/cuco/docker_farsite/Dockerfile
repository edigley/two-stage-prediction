FROM farsite0

RUN mkdir -p /FARSITE/OUTPUT
WORKDIR /FARSITE/

COPY farsite4P /FARSITE/
COPY credentials.json /FARSITE/

RUN apt-get update && \
  apt-get -y install python python-pip jq && \
  pip install awscli 

COPY work.sh /FARSITE/
COPY jsonenv.py /FARSITE/

ENTRYPOINT ./work.sh

