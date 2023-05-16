import datetime
import shutil
import sys

from pathlib import Path
from urllib import request

DIR = Path(__file__).parent


def main():
    build_dir = DIR / "builds/win32.win32.x86_64/Sophena"
    dist_dir = DIR / "dist"

    args = sys.argv
    if len(args) > 1 and args[1] == "clean":
        print("  delete old builds")
        if build_dir.parent.exists():
            shutil.rmtree(build_dir.parent)
        if dist_dir.exists():
            shutil.rmtree(dist_dir)
        print("done")
        return

    # check folders
    if not build_dir.exists():
        print("no build available; run the Eclipse export first")
        return
    if not dist_dir.exists():
        dist_dir.mkdir(parents=True)

    copy_jre_to(build_dir)
    v = f"{read_version()}_{datetime.date.today().isoformat()}"
    app_zip = dist_dir / f"Sophena_win_x64_{v}"
    print(f"  package {app_zip}")
    shutil.make_archive(app_zip.as_posix(), "zip", build_dir.parent.as_posix())
    print("all done")

def read_version() -> str:
    manifest = DIR.parent / "META-INF/MANIFEST.MF"
    with open(manifest, "r", encoding="utf-8") as f:
        for line in f:
            text = line.strip()
            if text.startswith("Bundle-Version"):
                return text.split(":")[1].strip()

    print("  could not detect app-version")
    return "no-version"


def copy_jre_to(build_dir: Path):
    jre_dir = build_dir / "jre"
    if jre_dir.exists():
        return
    jre_zip = DIR / "jre/jre_win_x64.zip"
    if not jre_zip.exists():
        fetch_jre(jre_zip)

    print(f"  copy JRE to {jre_dir}")
    shutil.unpack_archive(jre_zip, build_dir)
    extracted_dir = next(build_dir.glob("*jre*"))
    extracted_dir.rename(jre_dir)


def fetch_jre(target: Path):
    if target.exists():
        print(f"  {target} exists")
        return
    url = (
        "https://github.com/adoptium/temurin17-binaries/releases/download/"
        "jdk-17.0.6%2B10/OpenJDK17U-jre_x64_windows_hotspot_17.0.6_10.zip"
    )
    print(f"  download JRE from {url}")
    jre_dir = target.parent
    if not jre_dir.exists():
        jre_dir.mkdir(parents=True)
    request.urlretrieve(url, target)


if __name__ == "__main__":
    main()
