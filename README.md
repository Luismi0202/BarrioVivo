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

Teniendo ya la idea de mi app (lo que he realizado ha sido una app donde la gente pueda subir las comidas que le sobren y hayan unos administradores que controlen lo que se publica con una norma clara: hay que subir la fecha de caducidad del producto), lo que hice fue hacer un esquema de como quería distribuir las pantallas y luego me puse manos a la obra.

ESQUEMA QUE REALICÉ EN PAINT:

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/ESQUEMA_APP_IDEA.png)

El esquema representa lo que tenía en mi cabeza, dos usuarios distintos, uno normal y otro administrador, ambos entrarán en la pantalla principal que es la del login, una vez aquí se verificará si es usuario administrador o no (los usuarios administradores están declarados con correo y contraseña en el json como ya dije, los normales simplemente tienen que registrarse y se pondran en la base de datos de hilt/Rom en persistencia local). Si es administrador, su pantalla principal será el panel de control de admin, donde podrá ver todas las publicaciones y borrarlas si les parece inapropiadas (no siguen las reglas de poner la fecha de caducidad o simplemente no tiene nada que ver con comida). Si es usuario normal, podrá publicar comidas que le sobren, con descripciones foto etc. En las comidas cercanas, les saldrá solo las comidas que hay en su zona (que las ponen ellos a la hora de registrarse), cuando reclamas una comida podrás hablar con la otra persona para ver donde quedar para recogerla y poder acordar otras cosas. Se puede mandar audios, fotos etc, vamos un chat completo que siga muchas pautas de la rúbrica que estaba puesta. 

Las pantallas terminadas son las siguientes (todas las capturas están en la carpeta de CAPTURAS):

PANTALLA PRINCIPAL

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/CAPTURAS_APP/CREAR_CUENTA.jpeg)

PANTALLA INICIO SESIÓN

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/CAPTURAS_APP/INICIAR_SESION.jpeg)

PANTALLA REGISTRO

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/CAPTURAS_APP/CREAR_CUENTA.jpeg)

PANTALLA OLVIDÉ CONTRASEÑA

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/CAPTURAS_APP/PANTALLA_OLVIDE_CONTRASENIA.jpeg)

PANTALLA PANEL DE ADMIN

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/CAPTURAS_APP/PANTALLA_ADMIN.jpeg)

PANTALLA PRINCIPAL USUARIO

![](https://github.com/Luismi0202/BarrioVivo/blob/main/Capturas/CAPTURAS_APP/PANTALLA_USUARIO.jpeg)

### RA1.c - Uso de layouts y posicionamiento

El uso de layouts está presnete sobre todo en la sección ui/screen, ya que he usado la arquitectura MVVM, es decir, la parte lógica (Modelo) está aislada de la visual (Vista) pero se juntan en la carpeta de Vista-Modelo. Las screen simplemente son las diferentes pantalla de la aplicación y al final eso no tiene nada de lógica, si no que es meramente visual. Voy a poner como ejemplo de fragmento de código una de estas pantallas porque poner todas sería desarrollar muchísimo y al final todas siguen una estructura similar y se pueden ver dentro del propio código.

FRAGMENTO DE CÓDIGO DE PANTALLA VISTA USUARIO NORMAL

![](https://github.com/Luismi0202/BarrioVivo/blob/388e713ba4113b5e748bb623cd0ac8370129e0d7/app/src/main/java/com/example/barriovivo/ui/screen/HomeScreen.kt#L1-L453)

Voy a desglosar esto un poco para que se pueda entender que herramientas de posicionamiento he utilizado y como he organizado la interfaz.

https://github.com/Luismi0202/BarrioVivo/blob/388e713ba4113b5e748bb623cd0ac8370129e0d7/app/src/main/java/com/example/barriovivo/ui/screen/HomeScreen.kt#L80-L113

Lo primero es un Scaffold que es el cuerpo principal de la aplicación, en el defino la parte de arriba (topappbar) y los colores que tendrá y las acciones que se hará cuando se pulse uno de los botones que están en la topappbar (están las publicaciones globales y ver solo las tuyas)

Una vez dentro del Scaffold, el posicionamiento lo he ido haciendo con contenedores típicos de Jetpack Compose:

- Column: lo uso cuando quiero apilar elementos en vertical (títulos, botones y listas).
- Row: lo uso para poner elementos uno al lado del otro (por ejemplo icono + texto, acciones de una tarjeta, etc.).
- Box: lo uso para superponer contenido o alinear elementos dentro de un área.

La parte de posicionamiento más importante en Compose está en Modifier, porque es donde defino tamaños y espaciados. En mis pantallas se puede ver de forma constante el uso de:

- padding() para controlar márgenes internos
- fillMaxWidth() / fillMaxSize() para que un elemento ocupe el ancho o toda la pantalla
- weight() cuando necesito que varias columnas o filas se repartan el espacio
- height() / size() para tamaños concretos
- verticalScroll() y LazyColumn/LazyRow cuando tengo listas y contenido grande

Por ejemplo, en la pantalla de usuario el feed de publicaciones y los filtros se organizan con estas estructuras, y en la pantalla de admin hago algo similar pero orientado a moderación.

OTRAS PANTALLAS DONDE SE VE MUY BIEN EL LAYOUT Y LA JERARQUÍA VISUAL:

LOGIN/REGISTRO (AuthScreen)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AuthScreen.kt#L1-L220

CHAT (ChatScreen)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/ChatScreen.kt#L1-L260

DETALLE DE PUBLICACIÓN (MealDetailScreen)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/MealDetailScreen.kt#L1-L220

CREAR PUBLICACIÓN (CreateMealScreen)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/CreateMealScreen.kt#L1-L220

PANEL ADMIN (AdminDashboardScreen)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AdminDashboardScreen.kt#L1-L240

### RA1.d - Personalización de componentes

En mi caso no me he limitado a usar los componentes por defecto sin más, si no que he creado componentes reutilizables para mantener un estilo consistente en toda la aplicación. Esto se puede ver sobre todo en el archivo de componentes, donde tengo botones, cards, top bars, inputs y pequeños elementos visuales que se repiten en varias pantallas.

ARCHIVO PRINCIPAL DE COMPONENTES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/component/Components.kt#L1-L200

Además, la personalización global del estilo (colores y tipografías) está en el theme. Esto hace que todas las pantallas tengan una estética coherente.

COLORES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/theme/Color.kt

TIPOGRAFÍA
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/theme/Type.kt

TEMA
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/theme/Theme.kt#L1-L90

La idea ha sido que, por ejemplo, un botón principal o una card de publicación se vea igual en Home, en detalle y en admin. Así no tengo el diseño duplicado en cada pantalla y mantengo mejor el proyecto.

### RA1.e - Análisis del código

La estructura del proyecto sigue una separación clara por capas para que sea fácil de mantener:

- data: todo lo relacionado con persistencia (Room), DAOs, entidades y repositorios.
- domain: modelos de datos que uso en la app.
- ui: todo lo visual (pantallas, componentes, theme).
- ui/viewmodel: la parte que conecta la vista con los datos (MVVM).
- di: módulo de inyección de dependencias (Hilt) para que no tenga que instanciar manualmente la base de datos y los repositorios en cada parte.

Un punto importante es que el flujo típico para mostrar datos en pantalla es:

Room (DAO) -> Repository -> ViewModel -> Screen

Esto se ve muy claro por ejemplo en la parte de publicaciones:

DAO DE PUBLICACIONES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/database/dao/MealPostDao.kt#L1-L90

REPOSITORIO DE PUBLICACIONES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/repository/MealPostRepository.kt#L1-L200

VIEWMODEL PRINCIPAL (Home)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/viewmodel/HomeViewModel.kt#L1-L170

PANTALLA PRINCIPAL (Home)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/HomeScreen.kt#L1-L220

Otro ejemplo claro es el chat, que es de las partes más completas porque tiene conversaciones, mensajes y lógica de adjuntos:

DAO DE CONVERSACIONES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/database/dao/ChatConversationDao.kt#L1-L120

DAO DE MENSAJES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/database/dao/ChatMessageDao.kt#L1-L90

REPOSITORIO DE CHAT
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/repository/ChatRepository.kt#L1-L220

VIEWMODEL DE CHAT
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/viewmodel/ChatViewModel.kt#L1-L170

PANTALLA DE CHAT
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/ChatScreen.kt#L1-L260

### RA1.f - Modificación del código

Durante el desarrollo he ido modificando el código para ir metiendo funcionalidades reales y no dejarlo en una app de ejemplo. Los cambios más importantes han sido:

- Separar bien la lógica en repositorios y viewmodels para no tener pantallas con lógica mezclada.
- Añadir persistencia con Room para que usuarios, publicaciones, notificaciones y chat se mantengan entre reinicios.
- Añadir un sistema de admin separado del usuario normal (comprobación contra el json de admins).
- Mejorar el chat para que no sea solo texto y tenga adjuntos como audios o imágenes.

Al final, no es solo “hacer pantallas”, si no que todo está conectado con el flujo de datos que he comentado antes.

ARCHIVO DE APP PARA INICIALIZACIÓN Y RUTAS
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/BarrioVivoApp.kt#L1-L99

ACTIVITY PRINCIPAL Y NAVEGACIÓN
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/MainActivity.kt#L1-L336

### RA1.g - Asociación de eventos

La asociación de eventos en mi app es básicamente todo lo que pasa cuando el usuario pulsa un botón, escribe en un input, cambia un filtro o manda un mensaje. En Jetpack Compose esto se suele ver con callbacks (por ejemplo onClick) que llaman a funciones del ViewModel.

Ejemplos claros de eventos:

- Login/registro: al pulsar “iniciar sesión” o “crear cuenta” se llama al AuthViewModel para validar datos y registrar/loguear.
- Publicar comida: al pulsar “crear publicación” se valida el formulario y se guarda en la base de datos.
- Chat: enviar mensaje, grabar audio o adjuntar imagen son eventos que terminan creando un mensaje en el repositorio.
- Admin: borrar publicaciones es un evento que dispara una acción del AdminViewModel que actualiza la base de datos y refresca la lista.

VIEWMODEL DE AUTENTICACIÓN (EVENTOS DE LOGIN/REGISTRO)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/viewmodel/AuthViewModel.kt#L1-L302

PANTALLA DE AUTENTICACIÓN (BOTONES E INPUTS)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AuthScreen.kt#L1-L220

VIEWMODEL DE CREAR PUBLICACIÓN
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/viewmodel/CreateMealViewModel.kt#L1-L126

PANTALLA DE CREAR PUBLICACIÓN
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/CreateMealScreen.kt#L1-L220

### RA1.h - App integrada

La app está integrada porque todas las partes principales están conectadas entre sí y funcionan como un flujo real:

- Autenticación (usuarios normales en Room y admins en json)
- Home con publicaciones filtradas por zona
- Creación y detalle de publicaciones
- Notificaciones
- Chat entre usuarios cuando alguien reclama una comida
- Panel de admin para moderar

No son pantallas aisladas, si no que todo usa el mismo modelo de datos, la misma base de datos y la misma navegación. La navegación y el arranque de toda la app se gestiona desde la activity principal.

MAIN ACTIVITY
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/MainActivity.kt#L1-L336

---

## RA2 - Interfaces Naturales de Usuario (NUI)

### RA2.a - Herramientas NUI

Aunque mi app es principalmente una app clásica de móvil (pantallas táctiles), sí que he tenido en cuenta herramientas que se pueden considerar NUI porque permiten una interacción más natural:

- Micrófono: para mandar audios en el chat.
- Cámara: para adjuntar imágenes (por ejemplo en publicaciones o en chat).

A nivel de Android, esto implica trabajar con permisos y con APIs de sistema. No es algo “magia”, si no que hay que pedir el permiso y gestionar el resultado.

El eje principal de estas interacciones lo tengo dentro de la parte de chat, que es donde más sentido tiene mandar audio o imagen.

PANTALLA DE CHAT
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/ChatScreen.kt#L1-L260

### RA2.b - Diseño conceptual NUI

A nivel conceptual, en mi app las NUI tienen sentido donde realmente aportan y no por meterlas porque sí:

- En el chat: mandar audio o una foto es más natural que escribir un texto largo, sobre todo si estás quedando con alguien para recoger una comida.
- En crear publicación: adjuntar una foto del alimento es lo más directo para que el otro usuario vea si le interesa.

En mi caso he priorizado una interacción sencilla, es decir, que la app siga siendo usable aunque el usuario no use audio o cámara.

### RA2.c - Interacción por voz

La interacción por voz en mi app está representada por los mensajes de audio en el chat. Esto no es “un asistente de voz”, pero sí es interacción por voz real porque el usuario usa el micrófono para comunicarse de forma más rápida.

Lo importante aquí es que el audio se trate como un tipo de mensaje más, igual que un texto o una imagen, y que el chat tenga un diseño que lo soporte.

PANTALLA DE CHAT
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/ChatScreen.kt#L1-L260

REPOSITORIO DE CHAT (GESTIÓN DE MENSAJES)
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/repository/ChatRepository.kt#L1-L324

### RA2.d - Interacción por gesto

Ahora mismo no tengo implementada una interacción por gestos avanzada (tipo gestos con la cámara o sensores), pero sí que se podría incorporar de forma realista con cosas que son típicas en apps Android:

- Deslizar para acciones rápidas: por ejemplo en la lista de publicaciones permitir un swipe para “guardar”, “reclamar” o “reportar”.
- Deslizar para borrar: por ejemplo en notificaciones o en el panel de admin, para borrar una publicación con un gesto y confirmación.

Esto encaja muy bien porque en Compose se puede implementar con componentes de swipe (por ejemplo en Material) sin cambiar toda la arquitectura. Simplemente el gesto llamaría a un evento del ViewModel igual que un botón.

PANTALLA DE NOTIFICACIONES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/NotificationScreen.kt#L1-L171

PANEL ADMIN
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AdminDashboardScreen.kt#L1-L240

### RA2.e - Detección facial/corporal

No tengo detección facial/corporal implementada, pero podría incorporarse de forma muy concreta y útil, sin irse a algo irreal:

- Verificación básica al loguear como admin: por ejemplo, si un admin quiere entrar, se podría pedir una verificación extra con cámara (no tendría por qué ser reconocimiento facial completo, podría ser una comprobación de “liveness” o una foto para revisión, aunque esto ya sería más delicado por privacidad).
- Accesibilidad: detección simple de presencia o postura no tiene mucho sentido en mi caso, pero sí podría usarse para activar modo manos libres en el chat (por ejemplo, si detecta que el usuario está “mirando” la pantalla, reproducir audios automáticamente).

Una integración realista en Android suele hacerse con librerías tipo ML Kit (Google) para detección de cara/pose, y luego conectar el resultado a una acción de la UI. En mi caso tendría sentido limitarlo a algo opcional y bien explicado, porque si no añade complejidad y también temas de permisos y privacidad.

### RA2.f - Realidad aumentada

La realidad aumentada no está implementada en mi app, pero sí que se podría incluir de forma razonable sin cambiar el objetivo principal:

- Punto de recogida: cuando dos usuarios quedan para entregar una comida, se podría mostrar una vista opcional donde el usuario vea una flecha o marcador simple en la cámara para encontrar el punto (esto sería la típica idea, pero solo tendría sentido si también hay localización, y habría que justificarlo bien).
- Identificación visual: por ejemplo, en el detalle de una publicación se podría abrir una vista AR que muestre información flotante (nombre del alimento, caducidad) encima de la imagen o la cámara, pero esto sería más demostrativo que útil.

De forma realista, si se quisiera implementar, lo normal sería usar ARCore (Android) y limitarlo a una pantalla específica opcional, sin que dependa de ello el uso normal de la app.

---

## RA3 - Componentes Reutilizables

### RA3.a - Herramientas de componentes

La herramienta principal que he usado para componentes reutilizables es Jetpack Compose, porque en Compose todo son funciones composables que puedo reutilizar en distintas pantallas.

También he usado Material (componentes tipo Button, Card, TopAppBar, etc.) para no reinventar ruedas, y a partir de ahí he creado mis propios componentes con mi estilo.

ARCHIVO DE COMPONENTES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/component/Components.kt

### RA3.b - Componentes reutilizables

Los componentes reutilizables están en el archivo `Components.kt`. La idea es que tenga piezas que se repiten en varias pantallas, por ejemplo:

- Cards para publicaciones
- Botones principales/secundarios
- Inputs con estilos consistentes
- Pequeños elementos de UI para el chat

Esto evita que copie y pegue código en cada screen.

ARCHIVO DE COMPONENTES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/component/Components.kt#L1-L432

### RA3.c - Parámetros y defaults

En los componentes he intentado que estén pensados para reutilizarse, es decir, que acepten parámetros como:

- textos
- callbacks (acciones)
- estados (por ejemplo habilitado/deshabilitado)
- colores o variantes cuando tiene sentido

Y también usar valores por defecto para no tener que pasar siempre todo. Esto es importante porque hace que el componente sea flexible sin ser pesado de usar.

### RA3.d - Eventos en componentes

Los componentes no son solo “dibujos”, también tienen eventos. En Compose esto normalmente son parámetros tipo:

- onClick
- onValueChange

En mi app estos eventos terminan llamando a funciones del ViewModel de la pantalla, así el componente se mantiene reusable y no conoce la lógica interna.

### RA3.f - Documentación

La documentación de la parte de componentes está en:

- El propio nombre y organización de los componentes en `Components.kt`
- Este README, donde remarco qué archivo concentra la UI reusable

ARCHIVO DE COMPONENTES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/component/Components.kt

### RA3.h - Integración en la app

Los componentes reutilizables se usan en varias pantallas (Home, detalle, crear publicación, auth, admin, etc.). Se puede comprobar porque las screens importan composables del paquete `ui.component`.

HOME
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/HomeScreen.kt#L1-L454

DETALLE
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/MealDetailScreen.kt#L1-L655

CREAR PUBLICACIÓN
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/CreateMealScreen.kt#L1-L638

---

## RA4 - Usabilidad y Estándares

### RA4.a - Estándares

He intentado seguir estándares típicos de apps móviles:

- Navegación clara (pantalla de login -> home -> detalle -> chat, etc.)
- Uso de componentes Material para que se sienta como una app Android real
- Textos y botones con tamaños razonables
- Separación por capas (MVVM) para que el código sea mantenible

### RA4.b - Valoración de estándares

Seguir estándares me ha ayudado sobre todo a que la app sea coherente. Si cada pantalla tuviera estilos distintos o formas distintas de hacer lo mismo, para el usuario sería confuso.

Además, a nivel de código, la arquitectura MVVM hace que cada cosa tenga su sitio y sea más fácil detectar errores.

### RA4.c - Menús

En la app tengo navegación y acciones en barras superiores (top bars) y pantallas separadas por flujo. En Compose, el Scaffold con TopAppBar hace de “menú” principal a nivel de acciones, porque es donde pongo botones y opciones de filtrado.

EJEMPLO EN HOME
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/HomeScreen.kt

### RA4.d - Distribución de acciones

He intentado que las acciones importantes estén donde se esperan:

- Acciones globales arriba (top bar)
- Acciones de contenido cerca del contenido (por ejemplo botones en cards o abajo en formularios)
- En chat, accionar enviar/adjuntar donde está el input, porque es el flujo natural

### RA4.e - Distribución de controles

Los controles los organizo normalmente en Column con separaciones claras, y cuando hay formularios, agrupo inputs relacionados. Esto se ve sobre todo en auth y en crear publicación.

AUTH
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AuthScreen.kt

CREAR PUBLICACIÓN
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/CreateMealScreen.kt

### RA4.f - Elección de controles

Uso controles adecuados a cada cosa:

- TextField para inputs
- Botones para acciones
- Cards para representar publicaciones
- LazyColumn para listas

La idea es que el usuario lo entienda sin tener que aprender nada raro.

### RA4.g - Diseño visual

El diseño visual lo he hecho consistente usando un theme y componentes reutilizables. Así mantengo colores, tipografía y estilo iguales en toda la app.

THEME
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/theme/Theme.kt#L1-L90

### RA4.h - Claridad de mensajes

En pantallas como login/registro o crear publicación he intentado que los textos indiquen claramente qué se espera del usuario (por ejemplo correo, contraseña, zona, fecha de caducidad, etc.). Esto es importante porque si no, el usuario mete cualquier cosa y la app se vuelve frustrante.

### RA4.i - Pruebas de usabilidad

Las pruebas de usabilidad que he hecho han sido sobre todo manuales, es decir, ir probando pantallas y flujos completos:

- Registrarse
- Loguear
- Crear publicación
- Ver publicaciones y filtrar
- Reclamar y abrir chat
- Mandar mensajes y adjuntos
- Entrar como admin y borrar publicaciones

Esto me ha servido para ajustar cosas como tamaños de botones, textos, y orden de pantallas.

### RA4.j - Evaluación en dispositivos

He probado la app en emulador y también en un dispositivo Android para verificar que:

- La UI se adapta a la pantalla
- Los permisos (cámara/micrófono) se gestionan bien
- La base de datos mantiene la info al cerrar y abrir

---

## RA5 - Informes y Datos

En mi caso, la parte de datos está centrada en la base de datos local y en cómo se representan y consultan los datos de la app. Para el tema de informes, ahora mismo no hay un “informe” como documento final, pero sí hay consultas y pantallas que muestran datos agregados (por ejemplo listados de publicaciones, notificaciones y chat).

### RA5.a - Estructura del informe

Si tuviera que definir un informe dentro de mi app (por ejemplo para admin), la estructura lógica sería:

- Listado de publicaciones por estado (activas, reclamadas, expiradas)
- Datos de moderación (cuántas borradas, por qué motivo)
- Actividad de usuarios (publicaciones por zona)

Esto encaja con cómo ya tengo estructurados los datos en Room.

ENTIDADES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/database/entity/Entities.kt#L1-L196

### RA5.b - Generación de informes a partir de fuentes de datos

La fuente de datos principal es Room. A partir de los DAOs puedo obtener listados y luego mostrarlos en UI.

BASE DE DATOS
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/database/AppDatabase.kt#L1-L210

### RA5.c - Filtros sobre los valores a presentar

Los filtros se aplican a nivel de repositorio/viewmodel, y luego la pantalla pinta el resultado. Por ejemplo, en Home filtro por zona del usuario y también filtro por “mis publicaciones” o global.

HOME VIEWMODEL
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/viewmodel/HomeViewModel.kt#L1-L170

### RA5.d - Valores calculados, recuentos o totales

A día de hoy no tengo un módulo dedicado a cálculos tipo estadísticas, pero se podrían añadir recuentos fácilmente con consultas Room (por ejemplo contar publicaciones por zona o cuántas están caducadas). La estructura ya está preparada porque tengo DAOs y repositorios separados.

### RA5.e - Gráficos generados a partir de los datos

No tengo gráficos implementados en esta versión. Si se quisieran añadir, una forma realista sería:

- En el panel de admin poner un gráfico simple de publicaciones por zona o por día.
- Usar una librería de gráficos compatible con Compose.

---

## RA6 - Ayudas y Documentación

### RA6.a - Identificación de sistemas de generación de ayudas

En mi caso, el sistema de ayuda está centrado en documentación y mensajes dentro de la UI (textos, placeholders, validaciones). No tengo un sistema de ayuda externo, pero sí se puede considerar “ayuda” todo lo que guía al usuario.

### RA6.b - Generación de ayudas en formatos habituales

El formato habitual que tengo es:

- README con explicación del proyecto
- Textos dentro de la app (por ejemplo labels de inputs)
- Capturas en la carpeta `Capturas/`

### RA6.c - Ayudas sensibles al contexto

No tengo un sistema automatizado de ayuda contextual, pero sí hay ayudas implícitas en cada pantalla. Por ejemplo, en crear publicación, el usuario entiende que tiene que poner la fecha de caducidad porque el formulario se lo pide.

### RA6.d - Documentación de la estructura de información persistente

La persistencia se basa en Room. Tengo entidades claras y DAOs separados para cada tipo de dato.

ENTIDADES
https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/data/database/entity/Entities.kt#L1-L196

DAOS
https://github.com/Luismi0202/BarrioVivo/tree/main/app/src/main/java/com/example/barriovivo/data/database/dao

### RA6.e - Manual de usuario y guía de referencia

El manual de usuario, en mi caso, se puede construir con el flujo real de pantallas:

- Registrarse / iniciar sesión
- Entrar a Home y ver publicaciones
- Crear publicación
- Abrir una publicación y reclamar
- Abrir chat y acordar recogida
- Ver notificaciones

Las capturas de pantallas ya sirven como guía rápida.

CAPTURAS
https://github.com/Luismi0202/BarrioVivo/tree/main/Capturas/CAPTURAS_APP

### RA6.f - Manual técnico de instalación/configuración

A nivel técnico, la instalación/configuración se basa en:

- Abrir el proyecto en Android Studio
- Sincronizar Gradle
- Ejecutar en emulador o dispositivo

Las dependencias están definidas en el Gradle del módulo app.

GRADLE APP
https://github.com/Luismi0202/BarrioVivo/blob/main/app/build.gradle.kts

### RA6.g - Confección de tutoriales

Un tutorial corto (paso a paso) podría ser:

1. Crear cuenta
2. Crear una publicación con foto y fecha correcta
3. Desde otro usuario, reclamarla
4. Abrir el chat y mandar un audio
5. Como admin, borrar una publicación que no cumpla normas

---

## RA8 - Pruebas

### RA8.a - Estrategia de pruebas

Mi estrategia ha sido principalmente pruebas manuales por flujo y revisar estados límite:

- Campos vacíos en formularios
- Login incorrecto
- Publicaciones sin fecha
- Chat sin conexión entre usuarios (que no se rompa)
- Borrar publicaciones como admin

### RA8.b - Pruebas de integración

Las pruebas de integración más importantes son las que comprueban que todo el flujo Room -> Repository -> ViewModel -> UI funciona.

Por ejemplo:

- Creo una publicación en CreateMealScreen -> se guarda en Room -> aparece en Home.
- Reclamo una publicación -> se crea conversación -> aparece en Chat.

### RA8.g - Documentación de pruebas

La documentación de pruebas la hago describiendo los flujos probados (como en RA4.i y RA8.a). También las capturas ayudan a demostrar que la app funciona.

CAPTURAS
https://github.com/Luismi0202/BarrioVivo/tree/main/Capturas/CAPTURAS_APP

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
