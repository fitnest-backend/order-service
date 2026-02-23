#!/bin/bash
cd /Users/aousganeh/Desktop/fitnest/code/order-service/src/main/java/az/fitnest/order

# Wait, looking at the ls -R output from previously, the files are ALREADY moved into the new flat directories:
# config, controller, criteria, dto, entity, exception, repository, security, service
# Let me double check if there are any old directories left behind like `commerce`, `subscription`, `shared`
rm -rf commerce
rm -rf subscription
rm -rf shared

# Also fix the package declarations in all java files to point to the new flat structure
find . -type f -name "*.java" -exec sed -i '' \
  -e 's/package az\.fitnest\.order\.commerce\.\(.*\);/package az.fitnest.order.\1;/g' \
  -e 's/import az\.fitnest\.order\.commerce\.\(.*\);/import az.fitnest.order.\1;/g' \
  -e 's/package az\.fitnest\.order\.subscription\.api\.\(.*\);/package az.fitnest.order.\1;/g' \
  -e 's/import az\.fitnest\.order\.subscription\.api\.\(.*\);/import az.fitnest.order.\1;/g' \
  -e 's/package az\.fitnest\.order\.subscription\.domain\.\(.*\);/package az.fitnest.order.entity;/g' \
  -e 's/import az\.fitnest\.order\.subscription\.domain\.model\.\(.*\);/import az.fitnest.order.entity.\1;/g' \
  -e 's/package az\.fitnest\.order\.subscription\.adapter\.\(.*\);/package az.fitnest.order.\1;/g' \
  -e 's/import az\.fitnest\.order\.subscription\.adapter\.persistence\.\(.*\);/import az.fitnest.order.repository.\1;/g' \
  -e 's/import az\.fitnest\.order\.subscription\.adapter\.service\.\(.*\);/import az.fitnest.order.service.impl.\1;/g' \
  -e 's/package az\.fitnest\.order\.shared\.\(.*\);/package az.fitnest.order.\1;/g' \
  -e 's/import az\.fitnest\.order\.shared\.\(.*\);/import az.fitnest.order.\1;/g' \
  {} +

# Specific fix for service interfaces vs impl
find service/impl -type f -name "*.java" -exec sed -i '' -e 's/package az\.fitnest\.order\.service;/package az.fitnest.order.service.impl;/g' {} +
