#!/usr/bin/env bash

set -e

AH_OPERATOR="user"
AH_COMPANY="arrowhead"
AH_COUNTRY="eu"
AH_PASS_CERT="123456"
AH_CLOUD_NAME="testcloud1"

ah_cert () {
    dst_path=${1}
    dst_name=${2}
    cn=${3}

    file="${dst_path}/${dst_name}.p12"

    echo "Generating ${file}..."

    rm -f "${file}"

    keytool -genkeypair \
        -alias ${dst_name} \
        -keyalg RSA \
        -keysize 2048 \
        -dname "CN=${cn}, OU=${AH_OPERATOR}, O=${AH_COMPANY}, C=${AH_COUNTRY}" \
        -validity 3650 \
        -keypass ${AH_PASS_CERT} \
        -keystore ${file} \
        -storepass ${AH_PASS_CERT} \
        -storetype PKCS12 \
        -ext BasicConstraints:"Subject is a CA\nPath Length Constraint: None"
}

ah_cert_export () {
    src_path=${1}
    dst_name=${2}
    dst_path=${3}

    src_file="${src_path}/${dst_name}.p12"
    dst_file="${dst_path}/${dst_name}.crt"

    keytool -exportcert \
        -rfc \
        -alias ${dst_name} \
        -storepass ${AH_PASS_CERT} \
        -keystore ${src_file} \
    | openssl x509 \
        -out ${dst_file}
}

ah_cert_signed () {
    dst_path=${1}
    dst_name=${2}
    cn=${3}
    src_path=${4}
    src_name=${5}

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/${dst_name}.p12"

    ah_cert ${dst_path} ${dst_name} ${cn}

    keytool -export \
        -alias ${src_name} \
        -storepass ${AH_PASS_CERT} \
        -keystore ${src_file} \
    | keytool -import \
        -trustcacerts \
        -alias ${src_name} \
        -keystore ${dst_file} \
        -keypass ${AH_PASS_CERT} \
        -storepass ${AH_PASS_CERT} \
        -storetype PKCS12 \
        -noprompt

    keytool -certreq \
        -alias ${dst_name} \
        -keypass ${AH_PASS_CERT} \
        -keystore ${dst_file} \
        -storepass ${AH_PASS_CERT} \
    | keytool -gencert \
        -alias ${src_name} \
        -keypass ${AH_PASS_CERT} \
        -keystore ${src_file} \
        -storepass ${AH_PASS_CERT} \
    | keytool -importcert \
        -alias ${dst_name} \
        -keypass ${AH_PASS_CERT} \
        -keystore ${dst_file} \
        -storepass ${AH_PASS_CERT} \
        -noprompt
}

ah_cert_import () {
    src_path=${1}
    src_name=${2}
    dst_path=${3}
    dst_name=${4}

    src_file="${src_path}/${src_name}.crt"
    dst_file="${dst_path}/${dst_name}.p12"

    keytool -import \
        -trustcacerts \
        -file ${src_file} \
        -alias ${src_name} \
        -keystore ${dst_file} \
        -keypass ${AH_PASS_CERT} \
        -storepass ${AH_PASS_CERT} \
        -storetype PKCS12 \
        -noprompt
}

ah_cert_signed_system () {
    path="${1}/config/certificates"
    name=${2}
    file="${path}/${name}.p12"

    mkdir -p ${path}

    ah_cert_signed \
        "${path}" \
        ${name} \
        "${name}.${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" \
        . \
        ${AH_CLOUD_NAME}

    ah_cert_import . "master" "${path}" "${name}"
}

ah_cert_trust () {
    dst_path="${1}/config/certificates"
    src_path=${2}
    src_name=${3}

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/truststore.p12"

    rm -f "${dst_file}"

    keytool -export \
        -alias ${src_name} \
        -storepass ${AH_PASS_CERT} \
        -keystore ${src_file} \
    | keytool -import \
        -trustcacerts \
        -alias ${src_name} \
        -keystore ${dst_file} \
        -keypass ${AH_PASS_CERT} \
        -storepass ${AH_PASS_CERT} \
        -storetype PKCS12 \
        -noprompt
}

# Master certificate
ah_cert . master "arrowhead.eu"
ah_cert_export . master .

# Cloud certificate
ah_cert_signed . ${AH_CLOUD_NAME} "${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" . master

# Service Registry
ah_cert_signed_system serviceregistry_sql service_registry_sql
ah_cert_trust serviceregistry_sql . ${AH_CLOUD_NAME}

# Authorization
ah_cert_signed_system authorization authorization
ah_cert_trust authorization . ${AH_CLOUD_NAME}

# EventHandler
ah_cert_signed_system eventhandler event_handler
ah_cert_trust eventhandler . ${AH_CLOUD_NAME}

# Gatekeeper
ah_cert_signed_system gatekeeper gatekeeper
ah_cert_trust gatekeeper . ${AH_CLOUD_NAME}
cp "./${AH_CLOUD_NAME}.p12" "gatekeeper/config/certificates/"
ah_cert_export . master "gatekeeper/config/certificates/"

# Gateway
ah_cert_signed_system gateway gateway
ah_cert_trust gateway . ${AH_CLOUD_NAME}
ah_cert_export . master "gateway/config/certificates/"

# Orchestrator
ah_cert_signed_system orchestrator orchestrator
ah_cert_trust orchestrator . ${AH_CLOUD_NAME}
