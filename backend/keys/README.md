# JWT Keys

This project uses **RSA JWT signing (PKCS#8)** with SmallRye JWT (Quarkus).

JWT keys are stored locally for development purposes and **must not be committed to Git**.

## key Location
```txt
retailhub-serverapp
└── keys
    ├── privateKey.pem
    └── publicKey.pem
```

## Generate Keys (PKCS#8 format)

### Option 1: **OpenSSL 1.1.1**
```bash
# Generate temporary RSA key
openssl genrsa -out keys/temp.pem 2048

# Convert to PKCS#8
openssl pkcs8 -topk8 -inform PEM -in keys/temp.pem -out keys/privateKey.pem -nocrypt

# Generate public key
openssl rsa -in keys/privateKey.pem -pubout -out keys/publicKey.pem

# Remove temporary key
rm keys/temp.pem
```

### Option 2: **OpenSSL 3.x / Modern**
```bash
# Generate private key (PKCS#8)
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/privateKey.pem

# Generate public key
openssl rsa -in keys/privateKey.pem -pubout -out keys/publicKey.pem
```


## **NEVER commit these files to git!**