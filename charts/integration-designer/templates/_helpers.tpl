{{/* vim: set filetype=mustache: */}}

{{/*
Return the proper FlowXAppCamelCase image name
*/}}
{{- define "flowxApp.image" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.image "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper Docker Image Registry Secret Names
*/}}
{{- define "flowxApp.imagePullSecrets" -}}
{{ include "common.images.pullSecrets" (dict "images" (list .Values.image ) "global" .Values.global) }}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{/*
Expand the name of the chart.
*/}}
{{- define "name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "flowxApp.postgresql.fullname" -}}
{{- printf "%s-%s" .Release.Name "postgresql" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "flowxApp.mongodb.fullname" -}}
{{- printf "%s-%s" .Release.Name "mongodb" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Return whether to use the external DB or the built-in subcharts
*/}}
{{- define "flowxApp.useExternalDB" -}}
{{- if or (and (eq .Values.databaseType "mongodb") (not .Values.mongodb.enabled)) (and (eq .Values.databaseType "postgresql") (not .Values.postgresql.enabled)) -}}
  {{- true -}}
{{- end -}}
{{- end -}}

{{/*
Return whether to create an external secret containing the
credentials for the external database or not
*/}}
{{- define "flowxApp.createExternalDBSecret" -}}
{{- if and (include "flowxApp.useExternalDB" .) (not .Values.externalDatabase.existingSecret) -}}
  {{- true -}}
{{- end -}}
{{- end -}}

{{/*
Return the database host for flowxAppCamelCase
*/}}
{{- define "flowxApp.database.host" -}}
{{- if and (eq .Values.databaseType "mongodb") (.Values.mongodb.enabled) -}}
    {{- printf "%s" (include "flowxApp.mongodb.fullname" .) -}}
{{- else if and (eq .Values.databaseType "postgresql") (.Values.postgresql.enabled) -}}
    {{- printf "%s" (include "flowxApp.postgresql.fullname" .) -}}
{{- else }}
    {{- .Values.externalDatabase.host | quote }}
{{- end -}}
{{- end -}}

{{/*
Return the database port for flowxAppCamelCase
*/}}
{{- define "flowxApp.database.port" -}}
{{- if and (eq .Values.databaseType "mongodb") (.Values.mongodb.enabled) -}}
    {{- printf "3306" -}}
{{- else if and (eq .Values.databaseType "postgresql") (.Values.postgresql.enabled) -}}
    {{- printf "5432" -}}
{{- else }}
    {{- .Values.externalDatabase.port }}
{{- end -}}
{{- end -}}

{{/*
Return the database name for flowxAppCamelCase
*/}}
{{- define "flowxApp.database.name" -}}
{{- if and (eq .Values.databaseType "mongodb") (.Values.mongodb.enabled) -}}
    {{- .Values.mongodb.auth.database | quote }}
{{- else if and (eq .Values.databaseType "postgresql") (.Values.postgresql.enabled) -}}
    {{- .Values.postgresql.postgresqlDatabase | quote }}
{{- else }}
    {{- .Values.externalDatabase.name | quote }}
{{- end -}}
{{- end -}}

{{/*
Return the database username for flowxAppCamelCase
*/}}
{{- define "flowxApp.database.username" -}}
{{- if and (eq .Values.databaseType "mongodb") (.Values.mongodb.enabled) -}}
    {{- .Values.mongodb.auth.username | quote }}
{{- else if and (eq .Values.databaseType "postgresql") (.Values.postgresql.enabled) -}}
    {{- .Values.postgresql.postgresqlUsername | quote }}
{{- else }}
    {{- .Values.externalDatabase.user | quote }}
{{- end -}}
{{- end -}}

{{/*
Return the name of the database secret with its credentials
*/}}
{{- define "flowxApp.database.secretName" -}}
{{- if and (eq .Values.databaseType "mongodb") (.Values.mongodb.enabled) -}}
    {{- if .Values.mongodb.existingSecret -}}
        {{- printf "%s" .Values.mongodb.existingSecret -}}
    {{- else -}}
        {{- printf "%s" (include "flowxApp.mongodb.fullname" .) -}}
    {{- end -}}
{{- else if and (eq .Values.databaseType "postgresql") (.Values.postgresql.enabled) -}}
    {{- if .Values.postgresql.existingSecret -}}
        {{- printf "%s" .Values.postgresql.existingSecret -}}
    {{- else -}}
        {{- printf "%s" (include "flowxApp.postgresql.fullname" .) -}}
    {{- end -}}
{{- else -}}
    {{- if .Values.externalDatabase.existingSecret -}}
        {{- printf "%s" .Values.externalDatabase.existingSecret -}}
    {{- else -}}
        {{- printf "%s-%s" (include "common.names.fullname" .) "externaldb" -}}
    {{- end -}}
{{- end -}}
{{- end -}}
{{/*
Return  the proper Storage Class
*/}}
{{- define "flowxApp.storageClass" -}}
{{- include "common.storage.class" (dict "persistence" .Values.persistence "global" .Values.global) -}}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "flowxApp.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{ default (include "common.names.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
{{ default "default" .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return true if cert-manager required annotations for TLS signed certificates are set in the Ingress annotations
Ref: https://cert-manager.io/docs/usage/ingress/#supported-annotations
*/}}
{{- define "flowxApp.ingress.certManagerRequest" -}}
{{ if or (hasKey . "cert-manager.io/cluster-issuer") (hasKey . "cert-manager.io/issuer") }}
    {{- true -}}
{{- end -}}
{{- end -}}
