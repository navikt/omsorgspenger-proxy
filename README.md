# omsorgspenger-proxy

Proxy for at omsorgspenger-tjenester i gcp kan kalle tjenester i fss.
Tjenesten krever Azure-tokens og veksler til hva den bakomforliggende tjenesten trenger.

### Deployment
Push til main deployer til prod. Bruk 'ci skip' i commit-melding alternativt kjør 'Bygg og deploy' worfklow-action direkt mot branch for att deploye kun til dev.

## Tjenester dekker
* /infotrygd-grunnlag-paaroerende-sykdom (get) - Token må scopes til nevnte tjeneste, ikke proxy. Den støtter selv Azure-tokens.
* /k9-sak (kun enkelte endepunkt - se `K9SakRoute`)

### Bruk
omsorgspenger-proxy har en route per proxyet tjeneste, som beskrevet over. Alt etter dette i url'en blir proxyet videre.   
For eksempel vil et kall mot
`<omsorgspenger-proxy>/oppgave/min/path` bli proxyet til `<oppgave>/min/path`

### Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #sif_omsorgspenger.
