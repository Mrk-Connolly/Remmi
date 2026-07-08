#!/usr/bin/env python3
import zlib
import urllib.request
import urllib.parse
import os
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

PLANTUML_ALPHABET = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_'

def encode_plantuml(text):
    compressed = zlib.compress(text.encode('utf-8'))
    compressed = compressed[2:-4]
    result = ''
    for i in range(0, len(compressed), 3):
        b1 = compressed[i]
        b2 = compressed[i + 1] if i + 1 < len(compressed) else 0
        b3 = compressed[i + 2] if i + 2 < len(compressed) else 0
        result += PLANTUML_ALPHABET[(b1 >> 2) & 0x3F]
        result += PLANTUML_ALPHABET[((b1 << 4) | (b2 >> 4)) & 0x3F]
        result += PLANTUML_ALPHABET[((b2 << 2) | (b3 >> 6)) & 0x3F]
        result += PLANTUML_ALPHABET[b3 & 0x3F]
    return result

def render_puml(puml_path):
    base_name = os.path.splitext(os.path.basename(puml_path))[0]
    png_path = os.path.join(BASE_DIR, f"{base_name}.png")

    print(f"Rendering {puml_path} -> {png_path} ...", end=" ", flush=True)

    with open(puml_path, 'r', encoding='utf-8') as f:
        source = f.read()

    encoded = encode_plantuml(source)
    url = f"https://www.plantuml.com/plantuml/png/{encoded}"

    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=60) as response:
            with open(png_path, 'wb') as out:
                out.write(response.read())
        print(f"OK ({os.path.getsize(png_path)} bytes)")
        return True
    except Exception as e:
        print(f"ERROR: {e}")
        return False

if __name__ == "__main__":
    puml_files = [
        os.path.join(BASE_DIR, "modelo_completo.puml"),
        os.path.join(BASE_DIR, "herencia.puml"),
        os.path.join(BASE_DIR, "ai_analysis.puml"),
        os.path.join(BASE_DIR, "relaciones.puml"),
    ]

    success = 0
    for pf in puml_files:
        if render_puml(pf):
            success += 1

    print(f"\nDone: {success}/{len(puml_files)} rendered successfully")
    sys.exit(0 if success == len(puml_files) else 1)
