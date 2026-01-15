# BarrioVivo - Aplicación de Compartición de Comida

## 📋 Descripción del Proyecto

**BarrioVivo** es una aplicación móvil Android desarrollada con **Jetpack Compose** que permite a los usuarios compartir comidas que les sobran de forma gratuita con su comunidad local. La aplicación facilita la reducción de desperdicios de alimentos y fomenta la solidaridad entre vecinos.

## 🎯 Objetivos

- Reducir el desperdicio de alimentos
- Crear una comunidad más conectada y solidaria
- Permitir que personas compartan comidas sin costo alguno
- Mejorar la inclusión social y accesibilidad

## 🚀 Características Principales

### 1. **Sistema de Autenticación**
- Registro de usuarios con email, contraseña (con confirmación) y localización
- Login seguro con validación de credenciales
- Persistencia de sesión mediante DataStore
- Diferentes roles de usuario (Admin y Usuario Normal)

### 2. **Pantalla Principal (Home)**
- **Tab 1: Cerca de ti** - Muestra comidas aprobadas cercanas a tu ubicación (radio de 5km por defecto)
- **Tab 2: Mis Comidas** - Visualiza tus propias publicaciones con estado (pendiente, aprobada, rechazada)
- Bottom navigation con acceso a notificaciones y perfil
- Botón flotante para crear nuevas publicaciones

### 3. **Creación de Publicaciones**
- Captura de fotos desde cámara o selección de galería
- Campo obligatorio para nombre de la comida
- Descripción opcional
- **Advertencia roja obligatoria** sobre fecha de caducidad (requisito principal)
- Selector de ubicación
- Validación de fecha de caducidad (no puede ser anterior a hoy)

### 4. **Sistema de Moderación (Admin)**
- Dashboard de administrador para revisar publicaciones pendientes
- Opciones para aprobar o rechazar posts
- Comentarios del admin al rechazar
- Notificaciones automáticas a usuarios sobre decisiones

### 5. **Sistema de Notificaciones**
- Notificaciones cuando un post es aprobado
- Notificaciones cuando un post es rechazado (con motivo)
- Badge de notificaciones no leídas
- Historial de notificaciones

### 6. **Geolocalización**
- Almacenamiento de ubicación del usuario en el registro
- Filtrado de comidas cercanas basado en coordenadas GPS
- Soporte para principales ciudades españolas con coordinadas predefinidas

## 🏗️ Arquitectura Técnica

### Stack Tecnológico
- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose
- **Inyección de Dependencias:** Hilt
- **Persistencia Local:** Room Database
- **Preferencias:** DataStore
- **Navegación:** Jetpack Navigation Compose
- **Carga de Imágenes:** Coil
- **Ubicación:** Google Play Services Location
- **Permisos:** Accompanist Permissions
- **Logging:** Timber

### Estructura de Carpetas

```
app/src/main/
├── java/com/example/barriovivo/
│   ├── BarrioVivoApp.kt          # Application con Hilt
│   ├── MainActivity.kt            # Activity principal con NavGraph
│   ├── data/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt    # Base de datos Room
│   │   │   ├── DateTimeConverters.kt
│   │   │   ├── dao/              # Data Access Objects
│   │   │   └── entity/           # Entidades Room
│   │   ├── repository/           # Repositorios para acceso a datos
│   │   └── preferences/          # DataStore preferences
│   ├── domain/
│   │   ├── model/                # Data classes del dominio
│   │   └── validator/            # Validadores de negocio
│   ├── ui/
│   │   ├── screen/               # Pantallas Compose
│   │   ├── component/            # Componentes reutilizables
│   │   ├── viewmodel/            # ViewModels (MVVM)
│   │   └── theme/                # Tema y estilos
│   └── di/                       # Módulos Hilt
├── assets/
│   └── admin_config.json         # Configuración de administradores
└── res/
    └── values/                   # Strings, colors, themes
```

### Patrones de Diseño

1. **MVVM (Model-View-ViewModel)** para separación de responsabilidades
2. **Repository Pattern** para acceso a datos
3. **Dependency Injection** con Hilt para inyección de dependencias
4. **State Management** con StateFlow y MutableStateFlow

## 👥 Roles de Usuario

### Usuario Normal
- Registrarse con email, contraseña y localización
- Crear publicaciones de comidas
- Ver comidas cercanas
- Recibir notificaciones de aprobación/rechazo
- Actualizar su perfil

### Administrador
- Revisar publicaciones pendientes
- Aprobar o rechazar publicaciones
- Dejar comentarios en rechazos
- Ver historial de moderación

## 📱 Pantallas de la Aplicación

1. **AuthScreen** - Login y Registro
   - Tabs para cambiar entre login y registro
   - Validación de campos
   - Selector de ciudad

2. **HomeScreen** - Pantalla Principal
   - Tabs para "Cerca de ti" y "Mis Comidas"
   - FAB para crear comida
   - Bottom navigation

3. **CreateMealScreen** - Crear Publicación
   - Selector de foto (cámara/galería)
   - Campos obligatorios destacados
   - Advertencia sobre fecha de caducidad

4. **MealDetailScreen** - Detalle de Comida
   - Información completa del post
   - Botón para reclamar

5. **NotificationScreen** - Notificaciones
   - Historial de notificaciones
   - Marca como leído

6. **AdminDashboardScreen** - Panel de Admin
   - Publicaciones pendientes
   - Botones aprobar/rechazar

## 🔒 Seguridad y Permisos

### Permisos Implementados
- `CAMERA` - Captura de fotos
- `READ_EXTERNAL_STORAGE` - Acceso a galería
- `WRITE_EXTERNAL_STORAGE` - Almacenamiento de fotos
- `ACCESS_FINE_LOCATION` - Ubicación precisa
- `ACCESS_COARSE_LOCATION` - Ubicación aproximada
- `POST_NOTIFICATIONS` - Envío de notificaciones

### Validaciones
- Contraseñas hasheadas con SHA-256
- Validación de formato de email
- Validación de fecha de caducidad
- Validación de campos obligatorios

## 💾 Persistencia de Datos

### Room Database
- Tablas: Users, MealPosts, Notifications, Admins
- Relaciones entre entidades
- Migraciones para futuras versiones

### DataStore
- Almacenamiento de ID de usuario actual
- Rol del usuario
- Preferencias de la aplicación

### Assets
- `admin_config.json` - Lista de administradores precargada

## 📍 Geolocalización

- Cálculo de distancia entre dos puntos usando fórmula Haversine
- Radio predeterminado de 5km para comidas cercanas
- Soporte para ciudades españolas:
  - Madrid (40.4168, -3.7038)
  - Barcelona (41.3851, 2.1734)
  - Valencia (39.4699, -0.3763)
  - Sevilla (37.3891, -5.9844)
  - Bilbao (43.2630, -2.9350)

## 🎨 Diseño y Accesibilidad

### Paleta de Colores
- **Verde Primario:** #2ECC71
- **Naranja Secundario:** #F39C12
- **Rojo de Error:** #E74C3C
- **Fondos:** Colores claros y oscuros para contraste

### Componentes Reutilizables
- `BarrioVivoButton` - Botón estándar
- `BarrioVivoTextField` - Campo de texto con validación
- `ErrorMessage` - Mensaje de error desechable
- `ExpiryDateWarning` - Advertencia obligatoria
- `MealCard` - Tarjeta de comida

### Accesibilidad
- Descripciones de contenido en iconos
- Contraste suficiente entre texto y fondo
- Tamaños de texto legibles
- Navegación por tabs para usuarios de teclado

## 🚦 Estados de Publicación

1. **PENDING** - Esperando aprobación del admin
2. **APPROVED** - Aprobada y visible para usuarios
3. **REJECTED** - Rechazada (visible solo para autor)

## 📊 Base de Datos

### Entidades

#### Users
```
- id: String (PrimaryKey)
- email: String
- passwordHash: String
- city: String
- latitude: Double
- longitude: Double
- zipCode: String
- role: String (ADMIN/USER)
- createdAt: LocalDateTime
```

#### MealPosts
```
- id: String (PrimaryKey)
- userId: String (FK)
- userName: String
- title: String
- description: String
- photoUri: String
- expiryDate: LocalDate
- latitude: Double
- longitude: Double
- city: String
- createdAt: LocalDateTime
- status: String (PENDING/APPROVED/REJECTED)
- adminComment: String
```

#### Notifications
```
- id: String (PrimaryKey)
- userId: String (FK)
- title: String
- message: String
- type: String
- relatedPostId: String
- createdAt: LocalDateTime
- isRead: Boolean
```

#### Admins
```
- id: String (PrimaryKey)
- email: String
- userId: String (FK)
```

## 🔄 Flujos de Negocio

### Flujo de Registro
1. Usuario completa email, contraseña, confirmación y ciudad
2. Se validan los datos
3. Se crea usuario en Room
4. Se verifica si es admin (en admin_config.json)
5. Se asigna rol correspondiente
6. Se guarda sesión en DataStore

### Flujo de Crear Comida
1. Usuario selecciona foto (cámara/galería)
2. Completa datos: nombre, descripción, fecha caducidad
3. Se valida fecha (no anterior a hoy)
4. **Se muestra advertencia roja** sobre fecha de caducidad
5. Se guarda en Room con estado PENDING
6. Admin recibe notificación de nuevo post

### Flujo de Aprobación/Rechazo (Admin)
1. Admin ve lista de posts pendientes
2. Revisa cada publicación
3. Elige aprobar o rechazar
4. Si rechaza, añade comentario
5. Se notifica al usuario automáticamente
6. Post cambia de estado

### Flujo de Ver Comidas
1. Usuario va a Home > "Cerca de ti"
2. App obtiene su ubicación del registro
3. Filtra comidas aprobadas dentro de 5km
4. Ordena por fecha de creación
5. Usuario puede clicar para más detalles

## 📝 Notas de Desarrollo

### Configuración de Admin
Para añadir nuevos administradores, edita `assets/admin_config.json`:

```json
[
  {
    "id": "admin1",
    "email": "admin@barriovivo.com",
    "userId": "admin_user_1"
  }
]
```

### Permisos en Runtime
La app maneja permisos en Android 6.0+ mediante diálogos nativos y Accompanist Permissions.

### Compilación
```bash
./gradlew clean build
./gradlew installDebug
```

## 🎓 Requisitos del Proyecto Final Cumplidos

✅ **Varias pantallas o vistas bien estructuradas:** Auth, Home, CreateMeal, MealDetail, Notifications, AdminDashboard

✅ **Al menos 2 usuarios posibles:** Admin y Usuario Normal

✅ **Uso correcto de layouts:** Column, Row, Box, LazyColumn, Scaffold

✅ **Componentes reutilizables:** BarrioVivoButton, BarrioVivoTextField, ErrorMessage, ExpiryDateWarning, MealCard

✅ **Interacción con usuario:** Eventos en botones, selecciones, validaciones

✅ **Diseño visual coherente:** Paleta verde/naranja, tipografía consistente, jerarquía visual clara

✅ **Usabilidad y accesibilidad:** Descripciones en iconos, contraste, navegación clara

✅ **Documentación técnica y funcional:** README completo, código comentado, arquitectura clara

✅ **Persistencia con Hilt y Room:** Inyección de dependencias, base de datos local

✅ **Notificaciones:** Sistema de notificaciones automáticas

✅ **Permisos:** Manejo de cámara, galería y ubicación

✅ **Geolocalización:** Filtrado por ubicación cercana

✅ **Sistema Admin:** Aprobación/rechazo de posts con notificaciones

## 🚀 Próximas Mejoras (Futuro)

- Integración con backend (Firebase o API REST)
- Fotos en tiempo real desde cámara con preview
- Mapa interactivo de comidas cercanas
- Ratings y comentarios de usuarios
- Categorización de comidas
- Chat entre usuario y reclamante
- Historial de intercambios
- Estadísticas de impacto ambiental

## 📄 Licencia

Este proyecto es parte de un trabajo académico final.

## 👨‍💻 Autor

Desarrollo completamente realizado con Kotlin, Jetpack Compose y arquitectura moderna de Android.

---

**Última actualización:** Enero 2025

