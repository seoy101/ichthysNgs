FROM ubuntu:14.04
RUN apt-get update
RUN apt-get install -y unzip
RUN apt-get install -y vim
# java 1.8 

RUN  apt-get install -y software-properties-common
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

######

RUN mkdir /web
RUN mkdir /shellscript
VOLUME	/nfsdir

#RUN curl -O http://downloads.typesafe.com/typesafe-activator/1.3.10/typesafe-activator-1.3.10.zip
#RUN unzip typesafe-activator-1.3.10.zip -d / && rm typesafe-activator-1.3.10.zip && chmod 777 /activator-dist-1.3.10/bin/activator 


#docker private registry server (insecure mode)
RUN wget -qO- https://get.docker.com/ | sh

#sftp install
RUN apt-get install -y ssh

# nfs install
RUN apt-get install -y nfs-kernel-server

#add conf sh
ADD conf.sh /shellscript/
ADD services /shellscript/
ADD nfs-kernel-server /shellscript/

#add action sh

ADD launch.sh /bin/
ADD sftp-useradd.sh /bin/

#add web
ADD dev-1.0-SNAPSHOT /web/

#add exe
ADD bwa /nfsdir/
ADD MT.fa /nfsdir/


#ENV PATH $PATH:/activator-dist-1.3.10/bin/
WORKDIR /shellscript/
CMD ./conf.sh


#Clean up APT when done.
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
    apt-get autoclean && \
    apt-get autoremove -y && \
    rm -rf /var/lib/{apt,dpkg,cache,log}/




