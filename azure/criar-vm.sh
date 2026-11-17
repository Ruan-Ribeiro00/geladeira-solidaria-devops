#!/usr/bin/env bash
# =====================================================================
# Geladeira Solidaria Inteligente - criacao da VM no Azure (Azure CLI)
# Global Solution 2026/2 - CitizenTech - DevOps Tools & Cloud Computing
# =====================================================================
# Onde rodar: Azure Cloud Shell (portal.azure.com > icone ">_" > Bash),
# que ja vem com o Azure CLI instalado e logado na sua conta.
# Tambem roda no Git Bash/WSL/Linux com o Azure CLI instalado (faca "az login" antes).
#
#   bash criar-vm.sh
#
# O que o script cria:
#   1. Grupo de recursos         rg-geladeira-solidaria
#   2. VM Linux Ubuntu 24.04     vm-geladeira  (com Docker e Git ja instalados)
#      + rede virtual, IP publico e grupo de seguranca (criados pelo az vm create)
#   3. Regra liberando a porta 8080 (API) - a 22 (SSH) ja vem liberada
# =====================================================================

set -euo pipefail

# ---------------- Configuracao (pode alterar) ----------------
RM="rm567693"                                  # usado nas tags
GRUPO="rg-geladeira-solidaria"
VM="vm-geladeira"
USUARIO="azureuser"
IMAGEM="Canonical:ubuntu-24_04-lts:server:latest"
PORTA_API=8080
# Regiao: deixe vazio para o script escolher uma permitida na sua assinatura
REGIAO="${REGIAO:-}"
# Tamanhos testados em ordem (2 vCPU / 4 GB de RAM ou parecido)
TAMANHOS=("Standard_B2s" "Standard_B2als_v2" "Standard_B2as_v2" "Standard_D2s_v3")
# -------------------------------------------------------------

DIR="$(cd "$(dirname "$0")" && pwd)"
CLOUD_INIT="$DIR/cloud-init.yaml"

echo "==> Assinatura em uso:"
az account show --query "{nome:name, id:id}" -o table

# ---------- 1. Escolha da regiao ----------
# A assinatura Azure for Students so deixa criar recursos em algumas regioes
# (politica "Allowed resource deployment regions"). O script le essa lista.
if [ -z "$REGIAO" ]; then
  echo "==> Procurando as regioes permitidas na assinatura..."
  TODAS=$(az account list-locations --query "[].name" -o tsv)
  PERMITIDAS=$(az policy assignment list --disable-scope-strict-match --query "[].parameters" -o json 2>/dev/null \
    | grep -oE '"[a-z0-9]+"' | tr -d '"' | grep -Fxf <(echo "$TODAS") | sort -u || true)

  if [ -n "$PERMITIDAS" ]; then
    echo "    Regioes permitidas: $(echo $PERMITIDAS)"
    # Prefere as regioes mais perto do Brasil (menos atraso no video)
    for R in brazilsouth eastus2 eastus southcentralus centralus northcentralus westus2 westus3; do
      if echo "$PERMITIDAS" | grep -qx "$R"; then REGIAO="$R"; break; fi
    done
    [ -z "$REGIAO" ] && REGIAO=$(echo "$PERMITIDAS" | head -n 1)
  else
    echo "    Nenhuma restricao encontrada, usando brazilsouth."
    REGIAO="brazilsouth"
  fi
fi
echo "==> Regiao escolhida: $REGIAO"

# ---------- 2. Escolha do tamanho da VM ----------
echo "==> Verificando os tamanhos de VM disponiveis em $REGIAO (leva uns segundos)..."
TAMANHO=""
for T in "${TAMANHOS[@]}"; do
  OK=$(az vm list-skus -l "$REGIAO" --resource-type virtualMachines --size "$T" \
        --query "[?name=='$T' && length(restrictions)==\`0\`].name" -o tsv 2>/dev/null || true)
  if [ -n "$OK" ]; then TAMANHO="$T"; break; fi
done
if [ -z "$TAMANHO" ]; then
  echo "ERRO: nenhum dos tamanhos (${TAMANHOS[*]}) esta liberado em $REGIAO."
  echo "      Rode de novo escolhendo outra regiao, ex.:  REGIAO=eastus2 bash criar-vm.sh"
  exit 1
fi
echo "==> Tamanho escolhido: $TAMANHO"

# ---------- 3. Grupo de recursos ----------
echo "==> Criando o grupo de recursos $GRUPO..."
az group create --name "$GRUPO" --location "$REGIAO" \
  --tags projeto=geladeira-solidaria disciplina=devops rm="$RM" -o table

# ---------- 4. VM ----------
# --custom-data envia o cloud-init.yaml, que instala Docker, Compose e Git no primeiro boot.
echo "==> Criando a VM $VM (2 a 4 minutos)..."
az vm create \
  --resource-group "$GRUPO" \
  --name "$VM" \
  --location "$REGIAO" \
  --image "$IMAGEM" \
  --size "$TAMANHO" \
  --admin-username "$USUARIO" \
  --generate-ssh-keys \
  --public-ip-sku Standard \
  --os-disk-size-gb 30 \
  --custom-data "$CLOUD_INIT" \
  --tags projeto=geladeira-solidaria disciplina=devops rm="$RM" \
  -o table

# ---------- 5. Porta da API ----------
echo "==> Liberando a porta $PORTA_API no grupo de seguranca..."
az vm open-port --resource-group "$GRUPO" --name "$VM" \
  --port "$PORTA_API" --priority 1010 -o none

# ---------- 6. Resumo ----------
IP=$(az vm show -d -g "$GRUPO" -n "$VM" --query publicIps -o tsv)

echo
echo "=================================================================="
echo " VM criada!"
echo "   Grupo:   $GRUPO"
echo "   VM:      $VM ($TAMANHO, $REGIAO)"
echo "   IP:      $IP"
echo
echo " Proximos passos:"
echo "   1. Espere ~2 min (o cloud-init instala o Docker) e entre na VM:"
echo "        ssh $USUARIO@$IP"
echo "   2. Confira o Docker:   docker --version && docker compose version"
echo "   3. Clone o repositorio e suba os containers (veja o README)."
echo "   4. Swagger:  http://$IP:$PORTA_API/swagger-ui.html"
echo
echo " Depois de gravar o video, desligue a VM para nao gastar credito:"
echo "        az vm deallocate -g $GRUPO -n $VM"
echo "=================================================================="
