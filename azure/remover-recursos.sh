#!/usr/bin/env bash
# =====================================================================
# Liga, desliga ou apaga a VM da Geladeira Solidaria no Azure
#
#   bash remover-recursos.sh desligar   -> para a VM e para de cobrar a computacao
#   bash remover-recursos.sh ligar      -> liga de novo (o IP pode mudar)
#   bash remover-recursos.sh apagar     -> apaga TUDO (grupo de recursos inteiro)
# =====================================================================
set -euo pipefail

GRUPO="rg-geladeira-solidaria"
VM="vm-geladeira"

case "${1:-}" in
  desligar)
    az vm deallocate -g "$GRUPO" -n "$VM"
    echo "VM desligada (desalocada)." ;;
  ligar)
    az vm start -g "$GRUPO" -n "$VM"
    echo "VM ligada. IP: $(az vm show -d -g "$GRUPO" -n "$VM" --query publicIps -o tsv)" ;;
  apagar)
    read -r -p "Apagar o grupo $GRUPO e tudo dentro dele? (s/N) " R
    if [ "$R" = "s" ]; then
      az group delete -n "$GRUPO" --yes --no-wait
      echo "Exclusao iniciada (leva alguns minutos)."
    fi ;;
  *)
    echo "Uso: bash remover-recursos.sh [desligar|ligar|apagar]"; exit 1 ;;
esac
