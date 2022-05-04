# omsorgspenger-proxy

Proxy for at omsorgspenger-tjenester i gcp kan kalle tjenester i fss.
Tjenesten krever Azure-tokens og veksler til hva den bakomforliggende tjenesten trenger.

## Tjenester dekker
* /active-directory/me/memberOf (get) - Kun når Open-AM token er i bruk - ellers kan man hente det fra Azure token claim `groups`
* /dokarkivproxy (put)
* /infotrygd-grunnlag-paaroerende-sykdom (get) - Token må scopes til nevnte tjeneste, ikke proxy. Den støtter selv Azure-tokens.
* /k9-sak (kun enkelte endepunkt - se `K9SakRoute`)
* /open-am/keys (get) - Public endepunkt, krever ingen tokens.
* /oppgave (get & post)
* /saf/graphql (post)
* /sak (get & post)


### Bruk
omsorgspenger-proxy har en route per proxyet tjeneste, som beskrevet over. Alt etter dette i url'en blir proxyet videre.   
For eksempel vil et kall mot
`<omsorgspenger-proxy>/oppgave/min/path` bli proxyet til `<oppgave>/min/path`

### Applikasjoner som bruker proxy
#### omsorgspenger-journalforing
* oppgave
* dokarkivproxy
* saf
#### omsorgspenger-tilgangsstyring
* openam
#### omsorgsdager
* openam
#### k9-punsjbolle
* saf
* k9-sak
* infotrygd-grunnlag-paaroerende-sykdom
* sak

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #sif_omsorgspenger.
