import socket
import sys
import os
import threading
import logging
from datetime import datetime

s = socket.socket()
s.bind(("localhost",9999))
s.listen(40)
estandar = 1024

fecha = datetime.now()
date_time = fecha.strftime("%m-%d-%Y %H-%M-%S")
logging.basicConfig( level=logging.DEBUG, filename=date_time)
logger = logging.getLogger(date_time)
logger.setLevel(logging.DEBUG)
logger.info('Prueba: ' + date_time)

class Hilo(threading.Thread):

   contador = 0
   monitor = threading.Condition()


   def __init__(self, threadID):
      threading.Thread.__init__(self)
      self.threadID = threadID
      self.sc = None
      self.address = None
      self.waiting = threading.Condition()

   def run(self):
       #Recibe un cliente
       self.sc, self.address = s.accept()
       conf = self.sc.recv(50).decode(encoding='UTF-8', errors='strict')

       #synchronized - 
       Hilo.monitor.acquire()
       if conf == 'Established connection. Waiting for the message.':
           Hilo.contador += 1
           logger.info("Client "+ str(self.address) + " Status: "+ conf)
           print(self.threadID," está conectado al cliente ",self.address,", van ",Hilo.contador," clientes")
           
           if Hilo.contador >= limite:
               Hilo.monitor.notify_all()
           else:
               while Hilo.contador < limite:
                   Hilo.monitor.wait()
       print(self.threadID," salió de la espera ")
       Hilo.monitor.release()
       
       #Lee el texto
       f = open(dir,'rb')
       logger.info('Archivo seleccionado:'+ dir + " tamano"+ str(os.stat(dir).st_size))

       print("Comenzó a enviar")
       l = f.read(estandar)
       i=0
       hora_inicial = datetime.now().strftime("%m-%d-%Y %H:%M:%S").encode()
       self.sc.send(hora_inicial)
       while l:
           i+=1
           logger.info('Client connected: ' + str(self.address) + "package number: "+str(i))
           self.sc.send(l)
           l = f.read(estandar)
       f.close()
       print("terminó de enviar")

       """print("comenzó a esperar el mensaje")
       texto = self.sc.recv(32).decode(encoding='UTF-8')
       while not texto:
           texto = self.sc.recv(32).decode(encoding='UTF-8') 
           print("txto: "+texto)
       print("terminó de esperar")"""

       #conf1 = self.sc.recv(13).decode(encoding='UTF-8',errors='strict')
       #logger.info('Client connected: ' + str(self.address) + conf1)
       self.sc.close()

       #synchronized
       Hilo.monitor.acquire()
       Hilo.contador -= 1
       print(self.threadID," acaba de soltar al cliente ",self.address,", van ",Hilo.contador," clientes")
       if Hilo.contador == 0:
           s.close()
           print("San se acabó")
       Hilo.monitor.release()

var = int(input("¿Qué archivo desea enviar?\nIngrese '1' si desea enviar el archivo de 100MiB\nIngrese '2' si desea enviar el archivo de 250MiB "))

if var ==2:
    dir = "archivos/250mib.mp4"
elif var==1:
    dir = "archivos/100mib.pdf"
else:
    print("Recuerde que la respuesta debe ser '1' o '2'.")
    #Aquí se debe terminar la ejecución del programa
limite = int(input("Ingrese el número (entero) de clientes que quiere atender simultáneamente: "))

for i in range(limite):
    h = Hilo(i)
    h.start()