import json

backend_file = "c:/Users/Personal/Documents/.UCAB/PROYECTOS GITHUB/EntreNos/EntreNos_Motor_de_Intercambio/backend/data/usuarios.json"

with open(backend_file, 'r', encoding='utf-8') as f:
    users = json.load(f)

for u in users:
    if "catalogo" in u:
        u["catalogo"]["completado"] = False
        u["catalogo"]["habilidadesOfrecidas"] = []
        u["catalogo"]["necesidadesRegistradas"] = []

with open(backend_file, 'w', encoding='utf-8') as f:
    json.dump(users, f, indent=2, ensure_ascii=False)

print("Reset catalogos successful.")
