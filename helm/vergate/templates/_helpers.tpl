{{/*
Expand the name of the chart.
*/}}
{{- define "vergate.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "vergate.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart label.
*/}}
{{- define "vergate.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels.
*/}}
{{- define "vergate.labels" -}}
helm.sh/chart: {{ include "vergate.chart" . }}
{{ include "vergate.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels.
*/}}
{{- define "vergate.selectorLabels" -}}
app.kubernetes.io/name: {{ include "vergate.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Service account name.
*/}}
{{- define "vergate.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "vergate.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Name of the ESO-managed secret.
*/}}
{{- define "vergate.secretName" -}}
{{- printf "%s-secret" (include "vergate.fullname" .) }}
{{- end }}

{{/*
PostgreSQL service host (in-chart or external via ESO).
*/}}
{{- define "vergate.postgresHost" -}}
{{- printf "%s-postgresql" (include "vergate.fullname" .) }}
{{- end }}

{{/*
Valkey service host (in-chart or external via ESO).
*/}}
{{- define "vergate.valkeyHost" -}}
{{- printf "%s-valkey" (include "vergate.fullname" .) }}
{{- end }}
