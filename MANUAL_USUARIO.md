# Manual de usuario - BarrioVivo

Este manual es para explicar, paso a paso, cómo se usa la app BarrioVivo como usuario normal y como administrador. La idea de la aplicación es sencilla: en vez de tirar comida que te sobra, la publicas con fotos y fecha de caducidad y otra persona cercana la puede reclamar. Cuando alguien reclama una comida, se abre un chat para quedar y organizar la recogida.

## 1. Primer arranque y pantalla de autenticación

Cuando abres la app lo primero que te sale es la pantalla de autenticación (login/registro). Desde aquí puedes:

- Iniciar sesión con tu cuenta.
- Crear cuenta (registro).
- Recuperar contraseña.

Si inicias sesión con un usuario que está configurado como admin, te mandará directamente al panel de admin.
Capturas (rellenar):

- [ ] Captura login
- [ ] Captura registro
- [ ] Captura olvidé contraseña

## 2. Registro de usuario normal

Para registrarte tienes que poner:

- Email
- Contraseña y confirmación
- Ciudad y código postal
- Coordenadas (en mi caso está integrado en el formulario de la app)

La ubicación es importante porque el feed principal muestra comidas cercanas a tu zona.

## 3. Inicio de sesión

En el login pones tu email y contraseña.

- Si las credenciales están bien, entras.
- Si no, la app muestra un mensaje de error.

## 4. Pantalla principal de usuario (Home)

Cuando entras como usuario normal, llegas a la pantalla principal (Home). Aquí es donde se ve el feed de publicaciones.

Lo que se ve y se puede hacer:

- Ver publicaciones cercanas a tu ubicación.
- Cambiar entre publicaciones cercanas y tus publicaciones.
- Abrir una publicación para ver el detalle.
- Ir al chat (lista de conversaciones).
- Ir a notificaciones.
- Ir al perfil.
- Crear una publicación nueva.

Capturas (rellenar):

- [ ] Captura Home (usuario)
- [ ] Captura lista mis publicaciones

## 5. Crear una publicación

Para crear una publicación tienes que:

1. Darle a crear publicación.
2. Rellenar el nombre de la comida y descripción.
3. Añadir al menos una foto (obligatorio).
4. Poner fecha de caducidad (esto es clave en la app).
5. Confirmar.

Si falta algún dato importante (por ejemplo la foto), la app no deja publicar y te muestra el error.

Capturas (rellenar):

- [ ] Captura crear publicación (formulario)
- [ ] Captura selector/cámara

## 6. Ver detalle de una publicación

Al abrir una publicación se ve:

- Carrusel de fotos.
- Estado (disponible o reclamada).
- Descripción.
- Fecha de caducidad.

Acciones:

- Reclamar comida (si no es tuya y está disponible).
- Reportar publicación (si no es tuya).

Capturas (rellenar):

- [ ] Captura detalle publicación

## 7. Reclamar comida y chat

Cuando una persona reclama una comida:

- La publicación pasa a no disponible.
- Se crea una conversación de chat entre el creador y el reclamante.

En el chat se puede:

- Enviar mensajes de texto.
- Enviar fotos.
- Enviar audios.

La idea del chat es quedar y acordar la recogida de forma rápida.

Capturas (rellenar):

- [ ] Captura lista de chats
- [ ] Captura chat abierto
- [ ] Captura enviando audio

## 8. Notificaciones

Las notificaciones sirven para informar al usuario de eventos importantes, por ejemplo:

- Si alguien reclama tu comida.
- Si hay actividad relacionada con tus publicaciones.

Capturas (rellenar):

- [ ] Captura pantalla notificaciones

## 9. Perfil

En perfil puedes ver información de tu cuenta y según la configuración, cambiar contraseña u opciones relacionadas.

Capturas (rellenar):

- [ ] Captura perfil

## 10. Panel de administrador

Cuando entras como admin, tu pantalla principal es el panel de administrador. Desde aquí el admin puede:

- Ver todas las publicaciones.
- Ver publicaciones reportadas.
- Revisar motivos de reporte.
- Borrar publicaciones si no cumplen normas (por ejemplo publicaciones que no son comida o no tienen sentido).

Esto es lo que mantiene la app limpia y evita que la gente suba cualquier cosa.

Capturas (rellenar):

- [ ] Captura panel admin
- [ ] Captura lista reportados

## 11. Cierre de sesión

En las pantallas principales tienes la opción de cerrar sesión. Al hacerlo vuelves a la pantalla de login.

---

## Referencias de código (por si quiero justificar en la memoria)

Pantallas principales:

- Auth: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AuthScreen.kt#L1-L865
- Home: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/HomeScreen.kt#L1-L454
- Crear publicación: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/CreateMealScreen.kt#L1-L638
- Detalle: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/MealDetailScreen.kt#L1-L655
- Chat: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/ChatScreen.kt#L1-L944
- Notificaciones: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/NotificationScreen.kt#L1-L171
- Perfil: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/ProfileScreen.kt#L1-L393
- Admin: https://github.com/Luismi0202/BarrioVivo/blob/main/app/src/main/java/com/example/barriovivo/ui/screen/AdminDashboardScreen.kt#L1-L568
