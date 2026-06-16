import json

backend_file = "c:/Users/Personal/Documents/.UCAB/PROYECTOS GITHUB/EntreNos/EntreNos_Motor_de_Intercambio/backend/data/usuarios.json"

old_users = json.loads("""
[
  {
    "id": "USR-1003",
    "nombre": "Luis",
    "correoElectronico": "luis@alameda.com",
    "telefono": "0000000003",
    "descripcionPersonal": "Usuario Luis",
    "contrasenaHash": "$2a$10$UrDVA/nnEUow1OKAkMD0yO2vWryQfmESqx8.ZifhWepo07L.6WMyW",
    "version": 2,
    "monedero": {
      "creditosDisponibles": 200.0,
      "creditosRetenidos": 0.0,
      "creditosPorLogros": 0.0
    },
    "catalogo": {
      "completado": true,
      "habilidadesOfrecidas": [],
      "necesidadesRegistradas": []
    },
    "estado": "ACTIVO",
    "intentosFallidos": 0,
    "tiempoDesbloqueoMillis": 0,
    "primerIntentoFallidoMillis": 0,
    "reportesFraudeValidados": 0,
    "urlFotoPerfil": "default.png",
    "promedioCalificacion": 5.0,
    "cantidadCalificaciones": 1,
    "rol": "MIEMBRO_COMUNIDAD"
  },
  {
    "id": "USR-1002",
    "nombre": "Carlos",
    "correoElectronico": "carlos@alameda.com",
    "telefono": "0000000002",
    "descripcionPersonal": "Usuario Carlos",
    "contrasenaHash": "$2a$10$UrDVA/nnEUow1OKAkMD0yO2vWryQfmESqx8.ZifhWepo07L.6WMyW",
    "version": 0,
    "monedero": {
      "creditosDisponibles": 150.0,
      "creditosRetenidos": 0.0,
      "creditosPorLogros": 0.0
    },
    "catalogo": {
      "completado": true,
      "habilidadesOfrecidas": [],
      "necesidadesRegistradas": []
    },
    "estado": "ACTIVO",
    "intentosFallidos": 0,
    "tiempoDesbloqueoMillis": 0,
    "primerIntentoFallidoMillis": 0,
    "reportesFraudeValidados": 0,
    "urlFotoPerfil": "default.png",
    "promedioCalificacion": 5.0,
    "cantidadCalificaciones": 1,
    "rol": "MIEMBRO_COMUNIDAD"
  },
  {
    "id": "USR-1001",
    "nombre": "Usuario Generico",
    "correoElectronico": "usuario@alameda.com",
    "telefono": "0000000001",
    "descripcionPersonal": "Usuario de prueba",
    "contrasenaHash": "$2a$10$UrDVA/nnEUow1OKAkMD0yO2vWryQfmESqx8.ZifhWepo07L.6WMyW",
    "version": 0,
    "monedero": {
      "creditosDisponibles": 100.0,
      "creditosRetenidos": 0.0,
      "creditosPorLogros": 0.0
    },
    "catalogo": {
      "completado": true,
      "habilidadesOfrecidas": [],
      "necesidadesRegistradas": []
    },
    "estado": "ACTIVO",
    "intentosFallidos": 0,
    "tiempoDesbloqueoMillis": 0,
    "primerIntentoFallidoMillis": 0,
    "reportesFraudeValidados": 0,
    "urlFotoPerfil": "default.png",
    "promedioCalificacion": 5.0,
    "cantidadCalificaciones": 1,
    "rol": "MIEMBRO_COMUNIDAD"
  }
]
""")

with open(backend_file, 'r', encoding='utf-8') as f:
    current_users = json.load(f)

# Combine, prioritizing current_users (which has admin, jose, carlos3222, carlos8)
final_users = []
seen_ids = set()
seen_emails = set()

for u in current_users + old_users:
    if u['id'] not in seen_ids and u['correoElectronico'] not in seen_emails:
        seen_ids.add(u['id'])
        seen_emails.add(u['correoElectronico'])
        
        # Ensure password is correct and all have creditosPorLogros
        u['contrasenaHash'] = "$2a$10$UrDVA/nnEUow1OKAkMD0yO2vWryQfmESqx8.ZifhWepo07L.6WMyW"
        if "monedero" in u and "creditosPorLogros" not in u["monedero"]:
            u["monedero"]["creditosPorLogros"] = 0.0
            
        final_users.append(u)

with open(backend_file, 'w', encoding='utf-8') as f:
    json.dump(final_users, f, indent=2, ensure_ascii=False)

print("Merged users successfully. Total users:", len(final_users))
