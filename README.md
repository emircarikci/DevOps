# DevOps
DevOps Composes

Bu repo; NPM (reverse proxy), Postgres/PGAdmin, SonarQube, Keycloak, Jenkins, Penpot, OpenSearch stack, Harbor (hazır konfigürasyon gerektirir) ve uygulama servisleri (reserve-backend, reserve-frontend) içeren tek compose `docker-compose.yml` sunar.

## Kurulum
```bash
cp .env.example .env
# .env dosyasındaki gizli bilgileri doldur
docker network create dev-network || true
docker compose --env-file .env up -d
```

> **Gizlilik:** Tüm şifre/token gibi bilgileri `.env` içine koyulmalı. 

## Jenkins Deploy
- `Jenkinsfile` backend imajını build→push eder ve `reserve-backend` servisini **ENV override** ile günceller.
- Jenkins’te `registry-creds` (kullanıcı/şifre) oluştur.
- `Jenkins > Global Tool Configuration` içinde `Temurin-21` ve `Maven-3.9` tanımlı olmalı.

## Notlar
- Harbor servisleri host’ta `HARBOR_CFG_ROOT` altındaki config dosyalarını bekler (installer çıktıları). İstemezsen Harbor bloklarını compose’dan silebilirsin.
- `reserve-frontend` örnek amaçlıdır; gerçek frontend’i `frontend/dist` içine koy ya da pipeline ile build et.
- Keycloak proxy arkasında kolay kullanım için `KC_HOSTNAME_STRICT=false` ayarı açık.

## Güvenlik
- Varsayılan parolaları değiştir.
- Dış dünyaya açık portları (81, 443, 9000, 9080, 5601, 9200 vs.) güvenlik duvarıyla sınırla ve istenilen portalara göre düzenle.

## Örnek amacı
Buradaki örnek Tübitak'ta yapılan rezervasyon uygulaması için bir örnektir.
