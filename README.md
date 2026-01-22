# BarrioVivo - Documentación del Proyecto

## PRIMER APARTADO: PARTE DE CLASES

---

## RA1 - Interfaz Gráfica de Usuario


### RA1.a - Análisis de herramientas y librerías

Para la realización de mi idea, obviamente he tenido que usar una serie de herramientas para poder llevarla acabo. Lo más importante es que claramente se necesita un entorno de dearrollo para poder realizar un aplicacion móvil, en mi caso he utlizado Android Studio y como lenguaje de programación he usado Kotlin, ambas son las cosas que he aprendido a lo largo de este ciclo formativo para poder llevar a cabo una aplicación, así que no se sale para nada de lo aprendido.

Las librerías son códigos que ya están hechos que tu los importas en tu propio código para poder usarlo. En mi caso he utlizado libreris como icons para los iconos, hilt y rom para la persistencia local logrando hacer una base de datos en el propio móvil localmente y haciendo así un login y que los datos no se pierdan cuando se cierra la aplicación, también he usado android serialization para la serialización de objetos (es decir, transformar una cadena de texto en un objeto o viceversa), esto último sobre todo ha sido porque tengo un json dentro del propio código que es donde se encuentran los administradores y se encuentran serializados y el propio código los deserializa para transformarlos en usuarios administradores, haciendo que si loggeas con uno de los usuarios que están puestos en el json, abra como administrador. También he utilizado otras herramientas como pueden ser herramientas que te permiten usar la cámara del móvil o el micrófono para usarlos dentro de mi app. Esta clase de cosas no serían posible si no se dieran los permisos en el AndroidManifest

FRAGMENTOS DE CÓDIGO:

JSON ADMINISTRADORES

https://github.com/Luismi0202/BarrioVivo/blob/91a25f5e58ea925604030531db8f9e83d1f73c10/app/src/main/assets/admin_config.json#L1-L20

GRADLE CON IMPORTACIONES DE HERRAMIENTAS
https://github.com/Luismi0202/BarrioVivo/blob/91a25f5e58ea925604030531db8f9e83d1f73c10/app/build.gradle.kts#L1-L117

SETTINGS DE GRADLE CON LAS IMPORTACIONES NECESARIAS
https://github.com/Luismi0202/BarrioVivo/blob/91a25f5e58ea925604030531db8f9e83d1f73c10/build.gradle.kts#L1-L7

EJEMPLO DE UN CÓDIGO CON ALGUNAS IMPORTACIONES DE LIBRERÍA (EN TODOS LOS CÓDIGOS SE IMPORTAN COSAS, ADEMÁS SE PUEDE VER COMO IMPORTO MIS PROPIAS CLASES CREADAS DENTRO DE ESTE MISMO)

https://github.com/Luismi0202/BarrioVivo/blob/91a25f5e58ea925604030531db8f9e83d1f73c10/app/src/main/java/com/example/barriovivo/di/Modules.kt#L1-L25

### RA1.b - Creación de interfaz gráfica



### RA1.c - Uso de layouts y posicionamiento


### RA1.d - Personalización de componentes


### RA1.e - Análisis del código


### RA1.f - Modificación del código


### RA1.g - Asociación de eventos


### RA1.h - App integrada


---

## RA2 - Interfaces Naturales de Usuario (NUI)

### RA2.a - Herramientas NUI


### RA2.b - Diseño conceptual NUI


### RA2.c - Interacción por voz

    
### RA2.d - Interacción por gesto


### RA2.e - Detección facial/corporal


### RA2.f - Realidad aumentada


---

## RA3 - Componentes Reutilizables

### RA3.a - Herramientas de componentes


### RA3.b - Componentes reutilizables


### RA3.c - Parámetros y defaults


### RA3.d - Eventos en componentes


### RA3.f - Documentación


### RA3.h - Integración en la app


---

## RA4 - Usabilidad y Estándares

### RA4.a - Estándares


### RA4.b - Valoración de estándares


### RA4.c - Menús


### RA4.d - Distribución de acciones


### RA4.e - Distribución de controles


### RA4.f - Elección de controles


### RA4.g - Diseño visual


### RA4.h - Claridad de mensajes


### RA4.i - Pruebas de usabilidad


### RA4.j - Evaluación en dispositivos


---

## RA5 - Informes y Datos

### RA5.a - Estructura del informe


### RA5.b - Generación de informes a partir de fuentes de datos


### RA5.c - Filtros sobre los valores a presentar


### RA5.d - Valores calculados, recuentos o totales


### RA5.e - Gráficos generados a partir de los datos


---

## RA6 - Ayudas y Documentación

### RA6.a - Identificación de sistemas de generación de ayudas


### RA6.b - Generación de ayudas en formatos habituales


### RA6.c - Ayudas sensibles al contexto


### RA6.d - Documentación de la estructura de información persistente


### RA6.e - Manual de usuario y guía de referencia


### RA6.f - Manual técnico de instalación/configuración


### RA6.g - Confección de tutoriales


---

## RA8 - Pruebas

### RA8.a - Estrategia de pruebas


### RA8.b - Pruebas de integración


### RA8.g - Documentación de pruebas


---

---

# SEGUNDO APARTADO: PARTE DE EMPRESA

---

## RA5 - Informes (FFOE)

### RA5.f - Uso de herramientas para generar informes


### RA5.g - Modificación del código del informe


### RA5.h - App con informes integrados


---

## RA7 - Distribución de aplicaciones (FFOE)

### RA7.a - Empaquetado de la aplicación


### RA7.b - Personalización del instalador


### RA7.c - Paquete desde el entorno


### RA7.d - Herramientas externas


### RA7.e - Firma digital


### RA7.f - Instalación desatendida


### RA7.g - Desinstalación


### RA7.h - Canales de distribución


---

## RA8 - Pruebas avanzadas (FFOE)

### RA8.c - Pruebas de regresión


### RA8.d - Pruebas de volumen/estrés


### RA8.e - Pruebas de seguridad


### RA8.f - Uso de recursos


---

