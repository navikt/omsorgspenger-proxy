# omsorgspenger-proxy

Proxy for at omsorgspenger-tjenester i gcp kan kalle tjenester i fss  
Tjenester dekket:
* /pdl (post)
* /oppgave (get/post)

### Bruk
omsorgspenger-proxy har en route per proxyet tjeneste, som beskrevet over. Alt etter dette i url'en blir proxyet videre.   
For eksempel vil et kall mot
`<omsorgspenger-proxy>/oppgave/min/path` bli proxyet til `<oppgave>/min/path`

### Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #sif_omsorgspenger.
