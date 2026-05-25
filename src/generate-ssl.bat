@echo off
REM =============================================
REM  Ford SpecPulse — Geracao de certificado SSL
REM  Executa na raiz do projeto
REM =============================================

set KEYSTORE=src\main\resources\certs\specpulse.p12
set PASSWORD=specpulse123
set ALIAS=specpulse

if not exist "src\main\resources\certs" mkdir "src\main\resources\certs"

echo Gerando certificado SSL autoassinado...

keytool -genkeypair ^
  -alias %ALIAS% ^
  -keyalg RSA ^
  -keysize 2048 ^
  -storetype PKCS12 ^
  -keystore %KEYSTORE% ^
  -validity 365 ^
  -storepass %PASSWORD% ^
  -dname "CN=ford-spec-pulse,OU=FIAP,O=Ford,L=Sao Paulo,S=SP,C=BR"

if %ERRORLEVEL% == 0 (
    echo.
    echo Certificado gerado com sucesso: %KEYSTORE%
    echo Para ativar HTTPS, edite application.properties:
    echo   server.ssl.enabled=true
    echo   server.port=8443
) else (
    echo Erro ao gerar certificado. Verifique se o Java esta no PATH.
)